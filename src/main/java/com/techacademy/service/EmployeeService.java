package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

	private final ReportService reportService;
	private final EmployeeRepository employeeRepository;
	private final PasswordEncoder passwordEncoder;

	public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder,ReportService reportService) {
		this.employeeRepository = employeeRepository;
		this.passwordEncoder = passwordEncoder;
		this.reportService = reportService;
	}

	// 従業員保存
	@Transactional
	public ErrorKinds save(Employee employee) {

		// パスワードチェック
		ErrorKinds result = employeePasswordCheck(employee);
		if (ErrorKinds.CHECK_OK != result) {
			return result;
		}

		// 従業員番号重複チェック
		if (findByCode(employee.getCode()) != null) {
			return ErrorKinds.DUPLICATE_ERROR;
		}

		employee.setDeleteFlg(false);

		LocalDateTime now = LocalDateTime.now();
		employee.setCreatedAt(now);
		employee.setUpdatedAt(now);

		employeeRepository.save(employee);
		return ErrorKinds.SUCCESS;
	}

	// 従業員更新
	@Transactional
	public ErrorKinds update(Employee employee,String code) {
		Employee emp = findByCode(code);

		if("".equals(employee.getPassword())) {
			employee.setPassword(emp.getPassword());
			if(emp != null) {
				emp.getPassword();
			} else {
				// nullの場合の処理
				System.out.println("empはnullです");
			}
		}

		else {
			// パスワードの入力欄が空でない場合
			// パスワードチェックを行う(employeePasswordCheckメソッドを利用)
			// エラーの場合はemployeePasswordCheckメソッドの戻り値を返して終了
			ErrorKinds result = employeePasswordCheck(employee);
			if (ErrorKinds.CHECK_OK != result) {
				return result;
			}
		}

		employee.setDeleteFlg(false);

		LocalDateTime now = LocalDateTime.now();  // 現在の日時を取得
		employee.setCreatedAt(emp.getCreatedAt());
		employee.setUpdatedAt(now);

		employeeRepository.save(employee);  // 従業員オブジェクトをデータベースに保存
		return ErrorKinds.SUCCESS; // 更新成功

	}

	// 従業員削除
	@Transactional
	public ErrorKinds delete(String code, UserDetail userDetail) {

		// 自分を削除しようとした場合はエラーメッセージを表示
		if (code.equals(userDetail.getEmployee().getCode())) {
			return ErrorKinds.LOGINCHECK_ERROR;
		}
		Employee employee = findByCode(code);
		LocalDateTime now = LocalDateTime.now();
		employee.setUpdatedAt(now);
		employee.setDeleteFlg(true);

		// 削除対象の従業員（employee）に紐づいている、日報のリスト（reportList）を取得
        List<Report> reportList = reportService.findByEmployee(employee);

        // 日報のリスト（reportList）を拡張for文を使って繰り返し
        for (Report report : reportList) {
            // 日報（report）のIDを指定して、日報情報を削除
            reportService.delete(report.getId());
        }

		return ErrorKinds.SUCCESS;
	}

	// 従業員一覧表示処理
	public List<Employee> findAll() {
		return employeeRepository.findAll();
	}

	// 1件を検索
	public Employee findByCode(String code) {
		// findByIdで検索
		Optional<Employee> option = employeeRepository.findById(code);
		// 取得できなかった場合はnullを返す
		Employee employee = option.orElse(null);
		return employee;
	}

	// 従業員パスワードチェック
	private ErrorKinds employeePasswordCheck(Employee employee) {

		// 従業員パスワードの半角英数字チェック処理
		if (isHalfSizeCheckError(employee)) {

			return ErrorKinds.HALFSIZE_ERROR;
		}

		// 従業員パスワードの8文字～16文字チェック処理
		if (isOutOfRangePassword(employee)) {

			return ErrorKinds.RANGECHECK_ERROR;
		}

		employee.setPassword(passwordEncoder.encode(employee.getPassword()));

		return ErrorKinds.CHECK_OK;
	}

	// 従業員パスワードの半角英数字チェック処理
	private boolean isHalfSizeCheckError(Employee employee) {

		// 半角英数字チェック
		Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
		Matcher matcher = pattern.matcher(employee.getPassword());
		return !matcher.matches();
	}

	// 従業員パスワードの8文字～16文字チェック処理
	public boolean isOutOfRangePassword(Employee employee) {

		// 桁数チェック
		int passwordLength = employee.getPassword().length();
		return passwordLength < 8 || 16 < passwordLength;
	}

}
