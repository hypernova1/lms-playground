package org.sam.lms.api.config

import org.sam.lms.common.exception.ErrorCode
import org.sam.lms.common.exception.UnauthorizedException
import org.sam.lms.domain.account.domain.Provider
import org.sam.lms.domain.account.domain.RoleName
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * Gateway 에서 인증한 유저의 정보를 체크해서 핸들러에 주입해주는 클래스
 *  - 인증이 필요 없는 핸들러여도 인증된 유저를 구분해서 데이터를 제공해야할 때 사용
 * */
@Configuration
class ProviderArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType.equals(Provider::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        var provider: Provider? = null

        val userId: String? = webRequest.getHeader("X-User-ID")
        val userRole: String? = webRequest.getHeader("X-User-Role")

        if (userId != null && userRole != null) {
            provider = Provider(userId.toLong(), role = RoleName.from(userRole.removePrefix("ROLE_")))
        }

        val requiredAnnotations = listOf(
            RequireAuth::class.java,
            TeacherUser::class.java,
            StudentUser::class.java,
            AdminUser::class.java
        )

        when (provider?.role) {
            RoleName.TEACHER -> {
                if (!parameter.hasParameterAnnotation(TeacherUser::class.java)) {
                    throw UnauthorizedException(ErrorCode.UNAUTHORIZED_TOKEN)
                }
            }
            RoleName.STUDENT -> {
                if (!parameter.hasParameterAnnotation(StudentUser::class.java)) {
                    throw UnauthorizedException(ErrorCode.UNAUTHORIZED_TOKEN)
                }
            }
            RoleName.ADMIN -> {
                if (!parameter.hasParameterAnnotation(AdminUser::class.java)) {
                    throw UnauthorizedException(ErrorCode.UNAUTHORIZED_TOKEN)
                }
            }
            null -> {}
        }

        if (provider == null && requiredAnnotations.any { parameter.hasParameterAnnotation(it) }) {
            throw UnauthorizedException(ErrorCode.UNAUTHORIZED_TOKEN)
        }

        return provider
    }
}