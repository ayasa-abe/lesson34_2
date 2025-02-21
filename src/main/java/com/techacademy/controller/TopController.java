
package com.techacademy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TopController {

    // ログイン画面表示
    @GetMapping(value = "/login")
    public String login() {
        return "login/login";
    }

 // ログイン後のトップページ表示
    @GetMapping(value = "/")
    public String top() {

        // 修正前は、従業員一覧画面に当たる「/employees」にリダイレクト
        // return "redirect:/employees";

        // 修正後は、日報一覧画面に当たる「/reports」にリダイレクト
        return "redirect:/reports";
    }
}
