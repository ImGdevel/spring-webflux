package com.study.webflux.voice.v2;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TTS 바이너리 스트림을 지정한 크기만큼 묶어주는 서비스.
 */
@Service
public class AudioChunkingService {

	private final int defaultChunkSize;

	public AudioChunkingService(VoiceV2Properties properties) {
		this.defaultChunkSize = Math.max(0, properties.getChunkSize());
	}

	/**
	 * 설정된 기본 chunk 크기를 사용해 바이너리 스트림을 묶는다.
	 */
	public Flux<byte[]> chunk(Flux<byte[]> audioFlux) {
		return chunk(audioFlux, defaultChunkSize);
	}

	/**
	 * chunk 크기를 오버라이드해서 바이너리를 묶는다.
	 *
	 * @param audioFlux 원본 오디오 청크 스트림
	 * @param chunkSize 원하는 청크 크기 (0 이하이면 chunking 생략)
	 * @return 지정한 크기만큼 묶인 청크 스트림
	 */
	public Flux<byte[]> chunk(Flux<byte[]> audioFlux, int chunkSize) {
		if (chunkSize <= 0) {
			return audioFlux;
		}

		return Flux.defer(() -> {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			Flux<byte[]> chunked = audioFlux.concatMap(chunk -> {
				buffer.writeBytes(chunk);
				List<byte[]> ready = new ArrayList<>();

				while (buffer.size() >= chunkSize) {
					byte[] data = buffer.toByteArray();
					byte[] emit = Arrays.copyOf(data, chunkSize);
					ready.add(emit);

					buffer.reset();
					if (data.length > chunkSize) {
						buffer.write(data, chunkSize, data.length - chunkSize);
					}
				}

				return Flux.fromIterable(ready);
			});

			return chunked.concatWith(Mono.defer(() -> {
				if (buffer.size() > 0) {
					byte[] remaining = buffer.toByteArray();
					buffer.reset();
					return Mono.just(remaining);
				}
				return Mono.empty();
			}));
		});
	}
}

