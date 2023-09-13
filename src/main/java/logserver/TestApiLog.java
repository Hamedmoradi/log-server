package logserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestApiLog {



    @GetMapping("/test")
    @LogApi
    public Integer sumRes(@RequestParam("p1") String p1, String p2) {
        int res = sum(1, 2);

        return res;
    }

    public Integer sum(Integer a, Integer b) {
        return  a + b;
    }
}
