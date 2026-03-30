package com.example.skripsi.interfaces;

import java.util.List;

public interface IMasterDataService<Response, CreateRequest, UpdateRequest> {
    List<Response> getAll();

    Response create(CreateRequest request);

    Response update(Integer id, UpdateRequest request);
}
