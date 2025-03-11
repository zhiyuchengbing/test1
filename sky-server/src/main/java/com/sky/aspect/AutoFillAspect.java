package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面
 */
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {}


    @Before("autoFillPointcut")
    public void autoFill(JoinPoint joinPoint)  {
        log.info("开始公共字段自动填充");
        //获取到当前被拦截的方法的数据库操作类型
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对对象
        OperationType operationType = autoFill.value();

        //获取到当前被拦截的方法
        Object[] args = joinPoint.getArgs();
        if (args.length ==0) {
            return;
        }
        Object entity = args[0];
        //准备赋值数据
        LocalDateTime now = LocalDateTime.now();

        Long currentId = BaseContext.getCurrentId();

        if(operationType == OperationType.INSERT) {
            //4个赋值
            try {
                Method serCreatedTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method serUpdatedTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);

                Method serCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
                Method serUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                serCreatedTime.invoke(entity,now);

                serUpdatedTime.invoke(entity,now);
                serCreateUser.invoke(entity,currentId);
                serUpdateUser.invoke(entity,currentId);

            } catch (Exception e){
                e.printStackTrace();
            }
        }
        else if(operationType == OperationType.UPDATE) {
            try {
                Method serUpdatedTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method serUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                serUpdatedTime.invoke(entity,now);
                serUpdateUser.invoke(entity,currentId);

            } catch (Exception e){
                e.printStackTrace();

            }

        }



    }


}
