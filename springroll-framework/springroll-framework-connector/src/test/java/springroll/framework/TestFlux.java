package springroll.framework;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@RunWith(JUnit4.class)
public class TestFlux {
    private static Logger log = LoggerFactory.getLogger(TestFlux.class);

    Flux<String> source = Flux.just("foo", "bar", "baz", "qux", "quux", "corge", "grault", "garply", "waldo", "fred", "plugh", "xyzzy", "thud");

    @Test
    public void test1() {
        source.map(String::toUpperCase).zipWith(source.map(String::toLowerCase))
                .subscribe(tuple -> System.out.println(tuple.toString()));
    }

}
