import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorManagerImpl implements ExecutorManager {

    ExecutorService service = Executors.newCachedThreadPool();

    @Override
    public Context execute(Runnable callback, Runnable... tasks) {

        List<Future<Object>> list = new ArrayList<>();
        for (Runnable task : tasks) {
            Future<Object> submit = service.submit(Executors.callable(task));
            list.add(submit);
        }

        return new ContextImpl(service, callback, list);

    }
}
