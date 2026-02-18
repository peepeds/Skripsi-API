package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.ICompanyService;
import com.example.skripsi.models.WebResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("company")
public class CompanyController {

    private final ICompanyService companyService;

    public CompanyController(ICompanyService companyService){
       this.companyService = companyService;
    }

    @GetMapping("")
    public WebResponse<?> getCompanies(@RequestParam(value = "page", defaultValue = "0") int page,
                                                          @RequestParam(value = "limit", defaultValue = "15") int limit)
    {
        var results = companyService.getCompany(page,limit);
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get Companies data")
                .meta(results.getMeta())
                .result(results.getResult())
                .build();
    }


}
