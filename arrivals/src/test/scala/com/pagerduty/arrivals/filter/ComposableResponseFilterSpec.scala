package com.pagerduty.arrivals.filter

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import com.pagerduty.arrivals.api.filter.ResponseFilter
import org.scalatest.{FreeSpecLike, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class ComposableResponseFilterSpec extends FreeSpecLike with Matchers {

  implicit val ec = ExecutionContext.global

  val uri = Uri("/abc")
  val firstHeaderName = "X-TEST-FirstFilter"
  val secondHeaderName = "X-TEST-SecondFilter"

  object FirstFilter extends ResponseFilter[String] with ComposableResponseFilter[String] {
    def apply(request: HttpRequest, response: HttpResponse, data: String): Future[HttpResponse] = {
      Future.successful(response.addHeader(RawHeader(firstHeaderName, data)))
    }
  }

  "ComposableResponseFilter" - {
    "can be composed with another ResponseFilter of the same ResponseData type" in {

      object SecondFilter extends ResponseFilter[String] {
        def apply(request: HttpRequest, response: HttpResponse, data: String): Future[HttpResponse] = {
          Future.successful(response.addHeader(RawHeader(secondHeaderName, data)))
        }
      }

      val composedFilter = FirstFilter ~> SecondFilter

      val filterResult = Await.result(
        composedFilter.apply(HttpRequest(HttpMethods.GET, uri), HttpResponse(StatusCodes.OK), "test"),
        5.seconds
      )

      filterResult.headers.find(_.is(firstHeaderName.toLowerCase)) shouldEqual Some(RawHeader(firstHeaderName, "test"))
      filterResult.headers.find(_.is(secondHeaderName.toLowerCase)) shouldEqual Some(
        RawHeader(secondHeaderName, "test")
      )
    }
  }

  "can be composed with another ResponseFilter of different ResponseData type" in {

    object SecondFilter extends ResponseFilter[Any] {
      def apply(request: HttpRequest, response: HttpResponse, data: Any): Future[HttpResponse] = {
        Future.successful(response.addHeader(RawHeader(secondHeaderName, "test")))
      }
    }

    val composedFilter = FirstFilter ~> SecondFilter

    val filterResult = Await.result(
      composedFilter.apply(HttpRequest(HttpMethods.GET, uri), HttpResponse(StatusCodes.OK), "test"),
      5.seconds
    )

    filterResult.headers.find(_.is(firstHeaderName.toLowerCase)) shouldEqual Some(RawHeader(firstHeaderName, "test"))
    filterResult.headers.find(_.is(secondHeaderName.toLowerCase)) shouldEqual Some(RawHeader(secondHeaderName, "test"))
  }
}
