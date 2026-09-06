package microarch.delivery.core.application.jobs.quartz;

import microarch.delivery.core.application.commands.AssignOrderCommand;
import microarch.delivery.core.application.commands.AssignOrderCommandHandler;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution // Блокирует одновременное выполнение одной и той же задачи
public class AssignOrdersJob implements Job {
    private final AssignOrderCommandHandler useCase;
    private final AssignOrderCommand command = AssignOrderCommand.create().getValue();

    public AssignOrdersJob(AssignOrderCommandHandler useCase) {
        this.useCase = useCase;
    }

    @Override
    public void execute(JobExecutionContext context) {
        useCase.handle(command);
    }
}
