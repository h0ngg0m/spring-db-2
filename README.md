# 트랜잭션
스프링의 트랜잭션 AOP 기능은 public 메서드에만 트랜잭션을 적용하도록 기본 설정이 되어있다. 
그래서 protected, private, package-private 메서드에는 트랜잭션이 적용되지 않는다. 
주로 비즈니스 로직의 시작점에 걸기 때문에 대부분 외부에 열어준 곳을 시작점으로 사용한다. 이런 이유로 public 메서드에만 트랜잭션을 적용하는 것이다.

## 초기화 코드에는 트랜잭션이 적용되지 않는다
```java
static class Hello {

    @PostConstruct
    @Transactional
    public void init() {
        boolean isTxActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("Hello init isTxActive={}", isTxActive);
    }

}
```
왜냐하면 초기화 코드가 먼저 호출되고, 그 다음에 트랜잭션 AOP가 적용되지 않는다. 따라서 초기화 시점에는 해당 메서드에서 트랜잭션을 획득할 수 없다.

## 초기화 시 트랜잭션을 적용하는 방법
```java
static class Hello {

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init2() {
        boolean isTxActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("Hello init2 isTxActive={}", isTxActive);
    }

}
```
이 이벤트는 트랜잭션 AOP를 포함한 스프링이 컨테이너가 완전히 생성되고 난 다음에 이벤트가 붙은 메서드를 호출해준다. 따라서 init2는
트랜잭션이 적용된 것을 확인할 수 있다.

## 트랜잭션 옵션
### rollbackFor: 어떤 예외가 발생하면 롤백할 것인가 지정한다.
- 언체크 예외인 RuntimeException, Error와 그 하위 예외가 발생하면 `롤백한다.`
- 체크 예외인 Exception과 그 하위 예외들은 `커밋한다.`

이 옵션을 사용하면 기본 정책에 추가로 어떤 예외가 발생할 때 롤백할 지 지정할 수 있다.
```java
@Transactional(rollbackFor = Exception.class)
```
예를 들어 위처럼 지정하면 체크 예외인 Exception과 그 하위 예외들이 발생하면 롤백한다.

### noRollbackFor: rollbackFor와 반대로 어떤 예외가 발생하면 롤백하지 않을 것인가 지정한다.

### readOnly: 읽기 전용 트랜잭션으로 지정한다.
readOnly=true 옵션을 사용하면 읽기 전용 트랜잭션이 생성된다. 이 경우 등록, 수정, 삭제가 안되고 읽기 기능만 작동한다.
readOnly 옵션을 사용하면 읽기에서 다양한 성능 최적화가 발생할 수 있다.

## 예외와 트랜잭션 커밋, 롤백
- 트랜잭션은 런타임 예외가 발생하면 롤백한다. 하지만 체크 예외는 롤백하지 않는다.

트랜잭션 롤백, 커밋 로그 확인하기
```properties
logging.level.org.springframework.transaction.interceptor=TRACE
logging.level.org.springframework.jdbc.datasource.DataSourceTransactionManager=DEBUG

#JPA log
logging.level.org.springframework.orm.jpa.JpaTransactionManager=DEBUG
logging.level.org.hibernate.resource.transaction=DEBUG
```

스프링은 기본적으로 체크 예외는 비즈니스 의미가 있을 때 사용하고 런타임 예외는 복구 불가능한 예외로 가정한다.
- 체크 예외: 비즈니스 의미가 있을 때 사용
- 런타임 예외: 복구 불가능한 예외

## 트랜잭션 전파
<img width="797" alt="Screenshot 2024-05-22 at 11 11 56 AM" src="https://github.com/h0ngg0m/spring-db-2/assets/125632083/e2918ddc-9cfe-4e3a-b566-f60435dd139d">

### REQUIRES_NEW
외부 트랜잭션과 내부 트랜잭션을 완전히 분리해서 사용하는 방법에 대해서 알아보자.
외부 트랜잭션과 내부 트랜잭션을 완전히 분리해서 각각 별도의 물리 트랜잭션을 사용하는 방법이다. 그래서 커밋과 롤백도 각각 별도로
이루어지게 된다.
이 방법은 내부 트랜잭션에 문제가 발생해서 롤백해도, 외부 트랜잭션에는 영향을 주지 않는다. 반대로 외부 트랜잭션에서 문제가 발생해도 내부
트랜잭션에 영향을 주지 않는다. 이 방법을 사용하는 구체적인 예는 이후에 알아보고 지금은 작동 원리를 이해해보자.

<img width="838" alt="Screenshot 2024-05-22 at 12 00 28 PM" src="https://github.com/h0ngg0m/spring-db-2/assets/125632083/b69394da-244b-4450-8364-91fde9f8f9d3">
