package com.study.webflux.external;

public record JokeResponse(
	String id,
	String value,
	String url,
	String iconUrl
) {
}
