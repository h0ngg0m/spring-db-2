package hello.springdb2.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired CallService callService;

    @Test
    void printProxy() {
        log.info("aop class={}", callService.getClass());
    }

    @Test
    void internalCall() {
        callService.internal();
    }

    @Test
    void externalCall() {
        callService.external();
        /*
        external 내부에서 호출되는 internal 메소드는 트랜잭션이 적용되지 않는다.
        트랜잭션이 적용되지 않은 external 메소드는 트랜잭션 AOP의 프록시를 통해 호출되는 것이 아니고
        external 메소드 자체가 실행되고 external 안에서 호출되는 internal 메소드는 this.internal()로 호출되기 때문에
        트랜잭션이 적용되지 않는다.
         */
    }

    @TestConfiguration
    static class InternalCalkV1Config {


        @Bean
        CallService callService() {
            return new CallService();
        }
    }

    @Slf4j
    static class  CallService {

        public void external() {
            log.info("external call");
            printTxInfo();
            internal();
        }

        @Transactional
        public void internal() {
            log.info("internal call");
            printTxInfo();
        }


        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("txActive={}", txActive);
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("tx readOnly={}", readOnly);
        }

    }
}
