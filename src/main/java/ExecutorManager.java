public interface ExecutorManager {

    Context execute(Runnable callback, Runnable... tasks);

}