import java.util.List;
import java.util.concurrent.*;

public class ContextImpl implements Context {

    private final List<Future<Object>> futureList;
    private final ExecutorService service;
    private int count_failed = 0;
    private int count_interrupt = 0;
    private int count_complete = 0;
    private boolean finish = false;
    private volatile boolean complete_calculate = false;
    private Runnable callback;

    public ContextImpl(ExecutorService executorService, Runnable callback, List<Future<Object>> list) {
        this.service = executorService;
        this.futureList = list;
        this.callback = callback;
        new Thread(this::checkTerminated).start();
    }

    @Override
    public int getCompletedTaskCount() {
        return count_complete;
    }

    @Override
    public int getFailedTaskCount() {
        return count_failed;
    }

    @Override
    public int getInterruptedTaskCount() {
        return count_interrupt;
    }

    @Override
    public void interrupt() {
        finishExecute();
    }

    private void doFinalWork() {
        complete_calculate = true;
        service.shutdownNow();
        service.shutdown();
        new Thread(callback).start();
    }

    private void calculate() {
        for (Future<Object> objectFuture : futureList) {
            try {
                objectFuture.get(1, TimeUnit.NANOSECONDS);
                count_complete++;
            } catch (InterruptedException | TimeoutException e) {
                count_interrupt++;
            } catch (ExecutionException e) {
                count_failed++;
            }
        }
    }

    @Override
    public boolean isFinished() {
        return finish;
    }

    private void checkTerminated() {
        service.shutdown();
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
        finishExecute();
    }

    private void finishExecute() {
        synchronized (service) {
            if (!complete_calculate) {
                this.finish = true;
                calculate();
                doFinalWork();
            }
        }
    }
}
