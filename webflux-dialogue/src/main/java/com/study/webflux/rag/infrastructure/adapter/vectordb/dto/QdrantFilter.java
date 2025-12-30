package com.study.webflux.rag.infrastructure.adapter.vectordb.dto;

import java.util.List;

public record QdrantFilter(
	List<FilterCondition> must
) {
	public record FilterCondition(
		String key,
		MatchCondition match,
		RangeCondition range
	) {
		public static FilterCondition matchAny(String key, List<String> values) {
			return new FilterCondition(key, new MatchCondition(values), null);
		}

		public static FilterCondition rangeGte(String key, float value) {
			return new FilterCondition(key, null, new RangeCondition(value));
		}
	}

	public record MatchCondition(
		List<String> any
	) {
	}

	public record RangeCondition(
		float gte
	) {
	}
}
