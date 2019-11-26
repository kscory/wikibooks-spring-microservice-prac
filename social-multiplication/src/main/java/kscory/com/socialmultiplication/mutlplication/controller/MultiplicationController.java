package kscory.com.socialmultiplication.mutlplication.controller;


import kscory.com.socialmultiplication.mutlplication.domain.Multiplication;
import kscory.com.socialmultiplication.mutlplication.service.MultiplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/multiplications")
public class MultiplicationController {

    private final MultiplicationService multiplicationService;

    private final int serverPort;

    @Autowired
    public MultiplicationController(MultiplicationService multiplicationService,
                                    @Value("${server.port}") int serverPort) {
        this.multiplicationService = multiplicationService;
        this.serverPort = serverPort;
    }

    @GetMapping("/random")
    Multiplication getRandomMultiplication() {
        log.info("무작위 곱셈을 생성한 서버 @ {}", serverPort);
        return multiplicationService.createRandomMultiplication();
    }
}
