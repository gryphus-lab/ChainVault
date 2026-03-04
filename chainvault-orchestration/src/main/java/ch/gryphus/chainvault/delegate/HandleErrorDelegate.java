/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/** The type Handle error delegate. */
@Slf4j
@Component("handleError")
public class HandleErrorDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("HandleErrorDelegate executed for {}", execution.getProcessInstanceId());
    }
}
