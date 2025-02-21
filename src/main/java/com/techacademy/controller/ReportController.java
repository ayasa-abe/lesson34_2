package com.techacademy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {
	private final ReportService reportService;

	@Autowired
	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	// 日報一覧画面
	@GetMapping
	public String list(@AuthenticationPrincipal UserDetail userDetail, Model model) {

		//　ログインユーザー情報を取得する
		Employee loginUser = userDetail.getEmployee();

		List<Report> reportList= reportService.getReportsForCurrentUser(userDetail);
		// 現在のユーザーに関連するレポートのサイズを取得
		model.addAttribute("listSize", reportList.size());
		// レポートリストをモデルに追加
		model.addAttribute("reportList", reportList);
		return "reports/list";


	}

	// 日報詳細画面
	@GetMapping(value = "/{id}/")
	public String detail(@PathVariable("id") Integer id, Model model ) {
		//　パスパラメータで受け取った値をmodelに登録
		model.addAttribute("既に登録されている日付です");
		model.addAttribute("report", reportService.findById(id));

		return "reports/detail";
	}

	// 日報新規登録画面
	@GetMapping(value = "/add")
	public String create(Report report, Model model, @AuthenticationPrincipal UserDetail userDetail) {
		if (userDetail != null) {
			report = new Report();
			report.setEmployee(userDetail.getEmployee());
		}
		model.addAttribute("report",report);


		return "reports/new";
	}

	// 日報新規登録処理
	@PostMapping(value = "/add")
	public String add(@Validated Report report, BindingResult res, Model model,
			@AuthenticationPrincipal UserDetail userDetail) {

		if (res.hasErrors()) {
			return create(report, model, null);
		}

		ErrorKinds result = reportService.save(report, userDetail);

		if (ErrorMessage.contains(result)) {
			model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
			return create(report, model, null);
		}

		return "redirect:/reports";
	}

	// 日報更新画面
	@GetMapping(value = "/{id}/update")
	public String edit(@PathVariable("id") Integer integer, @ModelAttribute Report report, Model model) {
		if (integer != null) {
			model.addAttribute("report", reportService. findById(integer));
		}

		// 従業員更新画面に遷移
		return "reports/update";
	}

	// 日報更新処理
	@PostMapping(value = "/{id}/update")
	public String update(@PathVariable("id") Integer integer, @Validated Report report, BindingResult res, Model model, @AuthenticationPrincipal UserDetail userDetail) {

		// 入力チェック
		if (res.hasErrors()) {
			model.addAttribute("report", report);

			return edit(null,report,model);
		}
		report.setId(integer);
		// employeeServiceの更新処理を呼び出す
		ErrorKinds result = reportService.update(report,userDetail);
		if (ErrorMessage.contains(result)) {
			model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
			return edit(null, report, model);
		}

		return "redirect:/reports";
	}

	// 日報削除処理
	@PostMapping(value = "/{id}/delete")
	public String delete(@PathVariable("id") Integer id) {

		ErrorKinds result = reportService.delete(id);

		// 削除後は一覧画面にリダイレクト
		return "redirect:/reports";
	}

}

