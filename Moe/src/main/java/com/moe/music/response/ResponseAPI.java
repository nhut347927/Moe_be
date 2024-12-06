package com.moe.music.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseAPI<T> {
	private int code;
	private String message;
	private T data;
	private Map<String, String> errors;

	public static <T> ResponseAPI<T> success(T data, String message) {
		ResponseAPI<T> response = new ResponseAPI<>();
		response.setCode(200);
		response.setMessage(message);
		response.setData(data);
		return response;
	}

	public static ResponseAPI<Void> error(int code, String message, Map<String, String> errors) {
		ResponseAPI<Void> response = new ResponseAPI<>();
		response.setCode(code);
		response.setMessage(message);
		response.setErrors(errors);
		return response;
	}
}
