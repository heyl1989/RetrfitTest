package retrfit_test.cn.v1.retrfittest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import static rx.schedulers.Schedulers.io;

/**
 * Created by qy on 2018/1/29.
 */

public class RxJavaObserveOnMainThread {

    public static void main(String... args) {
        Scheduler observeOn = Schedulers.computation(); // Or use mainThread() for Android.

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://example.com")
                .addCallAdapterFactory(new ObserveOnMainCallAdapterFactory(observeOn))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(io()))
                .build();

        // 使用这个实例创建的服务将在“IO”调度器上执行。
        //并在“计算”调度器上通知他们的观察者。
    }

    static final class ObserveOnMainCallAdapterFactory extends CallAdapter.Factory {
        final Scheduler scheduler;

        ObserveOnMainCallAdapterFactory(Scheduler scheduler) {
            this.scheduler = scheduler;
        }

        @Override
        public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
            if (getRawType(returnType) != Observable.class) {
                return null; // 忽略不可观测类型。
            }

            // 查找下一个调用适配器，否则该适配器将被使用。
            //noinspection unchecked returnType以上检查是可观察的。
            final CallAdapter<Object, Observable<?>> delegate =
                    (CallAdapter<Object, Observable<?>>) retrofit.nextCallAdapter(this, returnType,
                            annotations);

            return new CallAdapter<Object, Object>() {
                @Override public Object adapt(Call<Object> call) {
                    // 代表得到正常的Observable ...
                    Observable<?> o = delegate.adapt(call);
                    //...并将其更改为向指定调度程序的观察者发送通知。
                    return o.observeOn(scheduler);
                }

                @Override public Type responseType() {
                    return delegate.responseType();
                }
            };
        }
    }
}
