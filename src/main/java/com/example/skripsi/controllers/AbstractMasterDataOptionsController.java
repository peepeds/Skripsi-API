package com.example.skripsi.controllers;

import com.example.skripsi.models.WebResponse;
import com.example.skripsi.interfaces.IMasterDataService;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.function.Supplier;

public abstract class AbstractMasterDataOptionsController<Service extends IMasterDataService<Response, CreateRequest, UpdateRequest>, Response, CreateRequest, UpdateRequest>
        extends AbstractMasterDataController<Service, Response, CreateRequest, UpdateRequest> {

    private final Supplier<?> optionsSupplier;
    private final String optionsMessage;

    protected AbstractMasterDataOptionsController(Service service, Supplier<?> optionsSupplier, String optionsMessage) {
        super(service);
        this.optionsSupplier = optionsSupplier;
        this.optionsMessage = optionsMessage;
    }

    @GetMapping("/options")
    protected WebResponse<?> getAllOptions() {
        var results = optionsSupplier.get();
        return WebResponse.builder()
                .success(true)
                .message(optionsMessage)
                .result(results)
                .build();
    }
}
