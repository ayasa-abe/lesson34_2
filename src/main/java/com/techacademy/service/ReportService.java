package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.catalina.User;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.entity.Employee.Role;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

	private final ReportRepository reportRepository;
	private final EmployeeRepository employeeRepository;

	public ReportService(ReportRepository reportRepository, EmployeeRepository employeeRepository) {
		this.reportRepository = reportRepository;
		this.employeeRepository = employeeRepository;
	}

	// 日報保存
	@Transactional
	public ErrorKinds save(Report report, UserDetail userDetail) {
		Employee loginEmployee = userDetail.getEmployee();

		// 同一日付重複チェック
		List <Report> reportList = findByEmployee(loginEmployee);
		if (reportList != null) {
			for (Report target : reportList) {
				if (target.getReportDate().equals(report.getReportDate())) {
					return ErrorKinds.DATECHECK_ERROR;
				}
			}
		}

		report.setEmployee(loginEmployee);
		report.setDeleteFlg(false);
		LocalDateTime now = LocalDateTime.now();
		report.setCreatedAt(now);
		report.setUpdatedAt(now);

		reportRepository.save(report);
		return ErrorKinds.SUCCESS;
	}

	// 日報更新
	@Transactional
	public ErrorKinds update(Report report, UserDetail userDetail) {
		Report originalReport = findById(report.getId());

		// 日報が存在するかを確認
		if (! report.getReportDate().equals(originalReport.getReportDate())) {
			List <Report> reportList = findByEmployee(originalReport.getEmployee());
			if (reportList != null) {
				for (Report target : reportList) {
					if (target.getReportDate().equals(report.getReportDate())) {
						return ErrorKinds.DATECHECK_ERROR;
					}
				}
			}
		}

		report.setEmployee(originalReport.getEmployee());
		report.setCreatedAt(originalReport.getCreatedAt());
		report.setDeleteFlg(false);
		// 更新日時をセット
		LocalDateTime now = LocalDateTime.now();
		report.setUpdatedAt(now);

		reportRepository.save(report);  // 更新処理
		return ErrorKinds.SUCCESS;
	}

	// 日報削除
	@Transactional
	public ErrorKinds delete(Integer reportId) {
		Report report = findById(reportId);

		if (report == null) {
			return ErrorKinds.DUPLICATE_ERROR;
		}

		report.setDeleteFlg(true); // 削除フラグを立てる
		report.setUpdatedAt(LocalDateTime.now());  // 更新日時をセット
		reportRepository.save(report);  // 保存して論理削除

		return ErrorKinds.SUCCESS;
	}


	// 日報一覧表示処理
	public List<Report> findAll() {
		return reportRepository.findAll();
	}

	// 1件を検索
	public Report findById(Integer id) {
		// findByIdで検索
		Optional<Report> option = reportRepository.findById(id);
		// 取得できなかった場合はnullを返す
		Report report = option.orElse(null);
		return report;
	}


	public List<Report> getReportsForCurrentUser(UserDetail userDetail) {

		Employee employee = userDetail.getEmployee();

		Role role = employee.getRole();


		// 管理者か一般ユーザーかを判定
		if (role == Employee.Role.ADMIN) {

			// 管理者権限がある場合、全ての日報を返す
			return reportRepository.findAll();
		} else {
			// 一般権限のユーザーの場合、自分が登録した日報のみ返す
			return reportRepository.findByEmployee(employee);
		}
	}

	public List<Report> findByEmployee(Employee employee){
		return reportRepository.findByEmployee(employee);
	}
}


