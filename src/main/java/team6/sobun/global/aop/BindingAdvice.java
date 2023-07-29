package team6.sobun.global.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.responseDto.ErrorResponse;


import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
public class BindingAdvice {

	private static final Logger log = LoggerFactory.getLogger(BindingAdvice.class);

	@Around("execution(* team6.sobun.domain.*Controller.*(..))")
	public Object validationHandler(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// 호출되는 메서드의 클래스 이름과 메서드 이름을 가져옵니다.
		String type = proceedingJoinPoint.getSignature().getDeclaringTypeName();
		String method = proceedingJoinPoint.getSignature().getName();

		log.info("메서드 '{}'가 '{}'에서 호출되었습니다.", method, type);

		// 메서드에 전달된 파라미터들을 가져옵니다.
		Object[] args = proceedingJoinPoint.getArgs();

		for (Object arg : args) {
			// 파라미터 중 BindingResult가 있는지 확인합니다. BindingResult는 유효성 검사 결과를 담고 있습니다.
			if (arg instanceof BindingResult) {
				BindingResult bindingResult = (BindingResult) arg;

				// 유효성 검사 오류가 있는지 확인합니다.
				if (bindingResult.hasErrors()) {
					Map<String, String> errorMap = new HashMap<>();

					// 유효성 검사 오류의 필드명과 메시지를 로그에 출력하고, errorMap에 저장합니다.
					for (FieldError error : bindingResult.getFieldErrors()) {
						log.warn("유효성 검사 오류: '{}'", error.getDefaultMessage());
						errorMap.put(error.getField(), error.getDefaultMessage());
					}

					// 유효성 검사 오류가 발생한 경우, ErrorResponse를 생성하고 ApiResponse에 담아 반환합니다.
					ErrorResponse errorResponse = new ErrorResponse("요청에 실패하였습니다.", HttpStatus.BAD_REQUEST);
					return ApiResponse.error(errorResponse);
				}
			}
		}

		// 유효성 검사 오류가 없는 경우, 해당 메서드를 정상적으로 실행하도록 합니다.
		return proceedingJoinPoint.proceed();
	}
}

