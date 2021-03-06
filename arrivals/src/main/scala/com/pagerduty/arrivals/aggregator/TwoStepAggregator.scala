package com.pagerduty.arrivals.aggregator

import com.pagerduty.arrivals.api

trait TwoStepAggregator[AuthData, RequestKey, AccumulatedState, AddressingConfig]
    extends api.aggregator.TwoStepAggregator[AuthData, RequestKey, AccumulatedState, AddressingConfig]
    with Aggregator[AuthData, RequestKey, AccumulatedState, AddressingConfig]
