# **1. Xác định yêu cầu** 

# **1.1 Mô hình hóa nghiệp vụ** 

- Nhân viên 

Nhận ca làm Chấm công ( check in – check out ) theo ngày Góp ý nếu chấm công lỗi 

- Quản lý 

Xếp ca cho nhân viên Thưởng cho nhân viên có thành tích Phạt nếu mắc lỗi 

Xử lý yêu cầu khiếu nại của nhân viên 

# **1.2 Xác định yêu cầu hệ thống** 

Công nghệ thông tin , phần mềm có thể hỗ trợ : 

# **Nhân viên:** 

- Hỗ trợ check-in/check-out trên phần mềm. 

- Hỗ trợ nhận và xem ca làm trên phần mềm. 

- Hỗ trợ xem lịch sử chấm công. 

- Gửi yêu cầu điều chỉnh khi thông tin chấm công bị sai hoặc thiếu. 

- Xem **thông tin tiền lương theo kỳ** . 

- Xem **chi tiết cách tính lương** , bao gồm số ngày/giờ làm, số ca làm, tiền thưởng, tiền phạt và các khoản điều chỉnh. 

# **Quản lý:** 

- Hỗ trợ xếp ca cho nhân viên. 

- Xem và xử lý yêu cầu điều chỉnh chấm công. 

- Thiết lập hoặc quản lý **mức lương/đơn giá theo ca** . 

- Ghi nhận thưởng cho nhân viên có thành tích. 

- Ghi nhận phạt khi nhân viên mắc lỗi. 

- Hỗ trợ **tính lương tự động dựa trên dữ liệu chấm công, ca làm, thưởng và phạt** . 

- Xem bảng lương của nhân viên theo từng kỳ. 

- Xác nhận/chốt bảng lương. 

# **1.3 Phân rã chức năng hệ thống** 

#  **Quản lý tài khoản** 

- Đăng nhập 

- Đăng xuất 

- Quản lý thông tin cá nhân 

- Phân quyền người dùng 

#  **Quản lý ca làm** 

- Xếp ca cho nhân viên 

- Xem ca làm 

- Nhận ca làm 

- Quản lý lịch làm việc 

#  **Quản lý chấm công** 

- Check-in 

- Check-out 

- Xem lịch sử chấm công 

- Theo dõi thời gian làm việc 

#  **Quản lý điều chỉnh chấm công** 

- Gửi yêu cầu điều chỉnh chấm công 

- Xem trạng thái yêu cầu 

- Xử lý yêu cầu điều chỉnh 

- Phê duyệt/từ chối yêu cầu 

#  **Quản lý thưởng và phạt** 

- Ghi nhận thưởng 

- Ghi nhận phạt 

- Xem lịch sử thưởng và phạt 

- Quản lý các khoản thưởng/phạt 

#  **Quản lý tiền lương** 

- Thiết lập đơn giá theo ca 

- Tổng hợp dữ liệu chấm công 

- Tính lương 

- Tính thưởng và phạt 

- Xem bảng lương 

- Xác nhận/chốt bảng lương 

#  **Báo cáo và thống kê** 

- Thống kê chấm công 

- Thống kê ca làm 

- Thống kê thưởng/phạt 

- Thống kê tiền lương 

# **1.4 Kịch bản** 

|Tên Usecase|**Xếp ca**|
|---|---|
|Tác nhân chính|Quản lý|
|Mức|2|
|Người chịu trách<br>nhiệm|Hệ thống<br>|
|Tiền điều kiện|Thiết bị phải kết nối mạng|
|Đảm bảo tối thiểu|Hệ thống không thay đổi trạng thái thông báo nếu thao<br>tác thất bại và thông báo nguyên nhân cho quản lý|
|Đảm bảo thành công|Lịch làm việc của nhân viên được cập nhật với ca làm<br>đã được quản lý chỉ định|
|Kích hoạt|Quản lýchọn chức năngxếpca|



Chuỗi sự kiện chính: 

1. Quản lý chọn chức năng "Xếp ca". 2. Hệ thống hiển thị danh sách nhân viên và thông tin lịch làm việc của nhân viên. 

3. Quản lý chọn nhân viên cần xếp ca. 

4. Hệ thống hiển thị các ngày có thể xếp ca. 5. Quản lý chọn ngày làm việc. 6. Hệ thống hiển thị danh sách các ca làm cố định trong ngày. 7. Quản lý lựa chọn ca làm cho nhân viên 8. Quản lý xác nhận thông tin xếp ca. 9. Hệ thống kiểm tra thông tin xếp ca. 

10. Nếu thông tin hợp lệ, hệ thống lưu lịch làm việc của nhân viên. 11. Hệ thống thông báo "Xếp ca thành công". <u>12. Hệ thống cập nhật lịch làm việc của nhân viên.</u> 

Luồng thay thế : 

- **2.a. Không tải được danh sách nhân viên** 

2.a.1. Hệ thống không thể lấy dữ liệu nhân viên. 

2.a.2. Hệ thống thông báo "Không thể tải danh sách nhân viên". 

2.a.3. Hệ thống không thay đổi dữ liệu. 

**8.a. Quản lý hủy thao tác** 8.a.1. Quản lý chọn "Hủy". 8.a.2. Hệ thống không lưu thông tin xếp ca. 

8.a.3. Hệ thống quay về màn hình trước đó. 

# **9.a. Thông tin xếp ca không hợp lệ** 

9.a.1. Hệ thống phát hiện thông tin xếp ca không hợp lệ. 

9.a.2. Hệ thống thông báo lỗi. 9.a.3. Hệ thống không thay đổi dữ liệu. 

9.a.4. Quản lý thực hiện lại thao tác. 

# **10.a. Lỗi khi lưu dữ liệu** 

10.a.1. Hệ thống xảy ra lỗi trong quá trình lưu lịch làm việc. 10.a.2. Hệ thống thông báo "Xếp ca thất bại". 10.a.3. Hệ thống không thay đổi dữ liệu. 

|Tên Usecase|**Chấm công**|
|---|---|
|Tác nhân chính|Nhân viên|
|Mức|2|



|Người chịu trách|Hệ thống|
|---|---|
|nhiệm||
|Tiền điều kiện|Thiết bị phải đăng nhập vào hệ thống và có kết nối<br>mạng và chia sẻ quyền truy cập vị trí|
|Đảm bảo tối thiểu|Hệ thống không thay đổi trạng thái thông báo nếu thao<br>tác thất bại và thông báo nguyên nhân cho nhân viên|
|Đảm bảo thành công|Dữ liệu chấm côngđượcghi vào hệ thốngnếu hợplệ|
|Kích hoạt|Nhân viên chọn chức năngchấm công|



Chuỗi sự kiện chính: 

1. Nhân viên chọn chức năng "Chấm công". 

2. Hệ thống xác định trạng thái chấm công hiện tại của nhân viên. 

3. Hệ thống lấy thời gian và vị trí hiện tại của thiết bị. 

4. Hệ thống kiểm tra thông tin ca làm việc của nhân viên trong ngày. 

5. Hệ thống kiểm tra điều kiện chấm công dựa trên ca làm việc và vị trí hiện tại của nhân viên. 

6. Nếu các điều kiện chấm công hợp lệ, hệ thống xác định thao tác cần thực hiện là Check-in hoặc Check-out. 

7. Nhân viên xác nhận thực hiện chấm công. 

8. Hệ thống ghi nhận dữ liệu chấm công, bao gồm thời gian và vị trí của nhân viên. 

9. Hệ thống thông báo "Chấm công thành công". 

<u>10.Hệ thống cập nhật trạng thái chấm công của nhân viên.</u> 

Luồng thay thế : 

# **2.a. Nhân viên chưa có ca làm việc** 

2.a.1. Hệ thống kiểm tra và phát hiện nhân viên chưa được xếp ca trong ngày. 2.a.2. Hệ thống thông báo "Nhân viên chưa được xếp ca". 

- 2.a.3. Hệ thống không ghi nhận dữ liệu chấm công. 

# **3.a. Không lấy được vị trí hiện tại** 

- 3.a.1. Hệ thống không thể lấy vị trí hiện tại của thiết bị. 

- 3.a.2. Hệ thống thông báo "Không thể xác định vị trí hiện tại". 

- 3.a.3. Hệ thống không ghi nhận dữ liệu chấm công. 

- **4.a. Nhân viên không có ca làm tại thời điểm chấm công** 

4.a.1. Hệ thống kiểm tra và phát hiện thời điểm chấm công không thuộc ca làm việc của nhân viên. 4.a.2. Hệ thống thông báo "Không có ca làm phù hợp tại thời điểm hiện tại". 4.a.3. Hệ thống không ghi nhận dữ liệu chấm công. **5.a. Vị trí của nhân viên không hợp lệ** 5.a.1. Hệ thống kiểm tra vị trí hiện tại của nhân viên. 5.a.2. Hệ thống phát hiện vị trí của nhân viên không đáp ứng điều kiện chấm công. 5.a.3. Hệ thống thông báo "Vị trí hiện tại không hợp lệ để chấm công". 5.a.4. Hệ thống không ghi nhận dữ liệu chấm công. **6.a. Nhân viên đã Check-in nhưng chưa Check-out** 6.a.1. Hệ thống phát hiện nhân viên đã thực hiện Check-in cho ca làm hiện tại. 6.a.2. Hệ thống xác định thao tác tiếp theo là Check-out. 6.a.3. Nhân viên thực hiện Check-out. 6.a.4. Hệ thống tiếp tục thực hiện từ bước 7. **6.b. Nhân viên chưa Check-in** 6.b.1. Hệ thống xác định thao tác cần thực hiện là Check-in. 6.b.2. Hệ thống tiếp tục thực hiện từ bước 7. **8.a. Lỗi khi lưu dữ liệu chấm công** 8.a.1. Hệ thống xảy ra lỗi trong quá trình lưu dữ liệu. 8.a.2. Hệ thống thông báo "Chấm công thất bại". 8.a.3. Hệ thống không thay đổi dữ liệu chấm công. 

|Tên Usecase|**Tính lương**|
|---|---|
|Tác nhân chính|Quản lý|
|Mức|2|
|Người chịu trách<br>nhiệm|Hệ thống<br>|
|Tiền điều kiện|Quản lý đã đăng nhập vào hệ thống, thiết bị có kết nối<br>mạng và đơn giá theo ca đã được thiết lập. Dữ liệu<br>chấm công của nhân viên trong kỳ lương đã có trên hệ<br>thống|
|Đảm bảo tối thiểu|Hệ thống không thay đổi dữ liệu tiền lương nếu quá<br>trình tính lương thất bại và thông báo nguyên nhân cho<br>Quản lý.|
|Đảm bảo thành công|Bảng lương của nhân viên trong kỳ được tính toán và<br>lưu vào hệ thống dựa trên dữ liệu chấm công, ca làm,<br>thưởng và phạt.|
|Kích hoạt|Quản lýchọn chức năngtính lương|



Chuỗi sự kiện chính: 

1. Quản lý chọn chức năng "Tính lương". 

2. Hệ thống hiển thị danh sách nhân viên. 3. Quản lý chọn nhân viên cần tính lương. 4. Hệ thống hiển thị thông tin làm việc của nhân viên trong tháng hiện tại 5. Quản lý chọn chức năng "Tính lương". 6. Hệ thống lấy thông tin các ca làm và đơn giá tương ứng của nhân viên trong tháng. 

7. Hệ thống tính lương cơ bản dựa trên số ca làm và đơn giá của từng ca. 8. Hệ thống hiển thị toàn bộ thông tin lương của nhân viên, bao gồm: 

- Lương cơ bản. 

- <u>Số ca làm.</u> 

- Số ca nghỉ có phép. 

- Số ca nghỉ không phép. 

- Tổng tiền thưởng. 

- Tổng tiền khấu trừ. 

- Tổng lương. 

- Ghi chú. 

9. Quản lý có thể lựa chọn thêm khoản thưởng hoặc khoản khấu trừ cho nhân viên. 

- 10.Nếu Quản lý thêm khoản thưởng hoặc khấu trừ, hệ thống thực hiện tạo và lưu khoản tương ứng. 

- 11.Hệ thống tính lại tổng tiền thưởng, tổng tiền khấu trừ và tổng lương của nhân viên. 

- 12.Hệ thống hiển thị lại thông tin lương sau khi cập nhật. 

- 13.Quản lý xác nhận thông tin lương. 

14.Hệ thống lưu thông tin lương của nhân viên trong tháng. 

15. Hệ thống thông báo "Tính lương thành công" 

Luồng thay thế : 

# **2.a. Không tải được danh sách nhân viên** 

2.a.1. Hệ thống không thể tải danh sách nhân viên. 

2.a.2. Hệ thống thông báo "Không thể tải danh sách nhân viên". 

2.a.3. Hệ thống không thay đổi dữ liệu. 

# **4.a. Không có dữ liệu làm việc trong tháng** 

4.a.1. Hệ thống không tìm thấy dữ liệu ca làm hoặc chấm công của nhân viên trong tháng hiện tại. 

4.a.2. Hệ thống thông báo "Không có dữ liệu làm việc trong tháng". 

4.a.3. Hệ thống không thực hiện tính lương. 

# **6.a. Không tìm thấy đơn giá của ca làm** 

6.a.1. Hệ thống phát hiện một hoặc nhiều ca làm của nhân viên chưa có đơn giá. 

6.a.2. Hệ thống thông báo "Chưa thiết lập đơn giá cho ca làm". 

6.a.3. Hệ thống không thực hiện tính lương. 

6.a.4. Quản lý thiết lập đơn giá và thực hiện lại thao tác. 

# **8.a. Không có khoản thưởng hoặc khấu trừ** 

8.a.1. Hệ thống không tìm thấy khoản thưởng hoặc khấu trừ nào của nhân viên trong tháng. 8.a.2. Hệ thống giữ tổng tiền thưởng và tổng tiền khấu trừ bằng 0. 8.a.3. Hệ thống tiếp tục thực hiện từ bước 10. 

# **12.a. Quản lý không thêm thưởng hoặc khấu trừ** 

12.a.1. Quản lý không lựa chọn thêm khoản thưởng hoặc khấu trừ. 

12.a.2. Hệ thống giữ nguyên các thông tin lương đã tính. 

12.a.3. Hệ thống tiếp tục thực hiện từ bước 15. 

# **12.b. Quản lý tạo khoản thưởng** 

12.b.1. Quản lý chọn chức năng "Thêm thưởng". 

12.b.2. Hệ thống hiển thị biểu mẫu tạo khoản thưởng. 

12.b.3. Quản lý nhập thông tin khoản thưởng. 

12.b.4. Quản lý xác nhận tạo khoản thưởng. 

12.b.5. Hệ thống kiểm tra thông tin khoản thưởng. 

12.b.6. Nếu thông tin hợp lệ, hệ thống tạo và lưu khoản thưởng cho nhân viên. 

12.b.7. Hệ thống tiếp tục thực hiện từ bước 14. 

**12.c. Quản lý tạo khoản khấu trừ** 12.c.1. Quản lý chọn chức năng "Thêm khấu trừ". 12.c.2. Hệ thống hiển thị biểu mẫu tạo khoản khấu trừ. 12.c.3. Quản lý nhập thông tin khoản khấu trừ. 

12.c.4. Quản lý xác nhận tạo khoản khấu trừ. 12.c.5. Hệ thống kiểm tra thông tin khoản khấu trừ. 12.c.6. Nếu thông tin hợp lệ, hệ thống tạo và lưu khoản khấu trừ cho nhân viên. 12.c.7. Hệ thống tiếp tục thực hiện từ bước 14. 

**12.d. Thông tin khoản thưởng không hợp lệ** 12.d.1. Hệ thống phát hiện thông tin khoản thưởng không hợp lệ. 12.d.2. Hệ thống thông báo lỗi. 12.d.3. Hệ thống không tạo khoản thưởng. 12.d.4. Quản lý thực hiện nhập lại thông tin. 12.e. Thông tin khoản khấu trừ không hợp lệ 12.e.1. Hệ thống phát hiện thông tin khoản khấu trừ không hợp lệ. 12.e.2. Hệ thống thông báo lỗi. 12.e.3. Hệ thống không tạo khoản khấu trừ. 12.e.4. Quản lý thực hiện nhập lại thông tin. 

**12.f. Lỗi khi lưu khoản thưởng hoặc khấu trừ** 12.f.1. Hệ thống xảy ra lỗi trong quá trình lưu khoản thưởng hoặc khấu trừ. 12.f.2. Hệ thống thông báo "Không thể lưu khoản thưởng/khấu trừ". 12.f.3. Hệ thống không thay đổi dữ liệu tương ứng. 

**16.a. Quản lý hủy thao tác** 16.a.1. Quản lý chọn "Hủy". 16.a.2. Hệ thống không lưu thông tin lương. 16.a.3. Hệ thống quay về màn hình trước đó. 

# **17.a. Lỗi khi lưu thông tin lương** 

17.a.1. Hệ thống xảy ra lỗi trong quá trình lưu thông tin lương. 

17.a.2. Hệ thống thông báo "Tính lương thất bại". 

17.a.3. Hệ thống không thay đổi dữ liệu lương 

|Tên Usecase|**Yêu cầu nghỉphép**|
|---|---|
|Tác nhân chính|Nhân viên|
|Mức|2|



|Người chịu trách|Hệ thống|
|---|---|
|nhiệm||
|Tiền điều kiện|Thiết bị phải kết nối mạng|
|Đảm bảo tối thiểu|Hệ thống không thay đổi dữ liệu nếu thao tác thất bại|
||và thông báo nguyên nhân cho nhân viên|
|Đảm bảo thành công|Yêu cầu nghỉ phép của nhân viên được lưu vào hệ<br>thống với trạng thái "Chờ phê duyệt" và gửi thông báo<br>đến quản lý|
|Kích hoạt|Quản lýchọn chức năng yêu cầu nghỉphép|



# Chuỗi sự kiện chính: 

1. Nhân viên chọn chức năng "Yêu cầu nghỉ phép". 2. Hệ thống hiển thị danh sách các ca làm việc của nhân viên có thể xin nghỉ. 

3. Nhân viên chọn ca làm việc cần xin nghỉ. 4. Hệ thống hiển thị thông tin ca làm việc đã chọn và biểu mẫu yêu cầu nghỉ phép. 

5. Nhân viên nhập lý do nghỉ phép và các thông tin cần thiết. 6. Nhân viên xác nhận gửi yêu cầu nghỉ phép. 7. Hệ thống kiểm tra thông tin yêu cầu nghỉ phép. 8. Nếu thông tin hợp lệ , hệ thống gửi thông báo đến quản lý về yêu cầu nghỉ phép mới. 

9. Hệ thống thông báo "Gửi yêu cầu nghỉ phép thành công". 

- <u>10.Hệ thống hiển thị trạng thái yêu cầu "Chờ phê duyệt" cho nhân viên.</u> 

Luồng thay thế : 

# **2.a. Không có ca làm việc có thể yêu cầu nghỉ** 

2.a.1. Hệ thống kiểm tra và phát hiện nhân viên không có ca làm việc phù hợp để yêu cầu nghỉ. 

2.a.2. Hệ thống thông báo "Không có ca làm việc có thể yêu cầu nghỉ". 

2.a.3. Hệ thống không tạo yêu cầu nghỉ phép. 

# **2.b. Không tải được danh sách ca làm việc** 

2.b.1. Hệ thống không thể lấy dữ liệu ca làm việc của nhân viên. 

2.b.2. Hệ thống thông báo "Không thể tải danh sách ca làm việc". 2.b.3. Hệ thống không thay đổi dữ liệu. 

**5.a. Nhân viên chưa nhập đầy đủ thông tin** 5.a.1. Hệ thống phát hiện thông tin yêu cầu nghỉ phép chưa đầy đủ. 5.a.2. Hệ thống thông báo các thông tin còn thiếu. 5.a.3. Nhân viên bổ sung thông tin và thực hiện lại thao tác. **6.a. Nhân viên hủy thao tác** 6.a.1. Nhân viên chọn "Hủy". 6.a.2. Hệ thống không tạo yêu cầu nghỉ phép. 6.a.3. Hệ thống quay về màn hình trước đó. **7.a. Thông tin yêu cầu nghỉ phép không hợp lệ** 7.a.1. Hệ thống phát hiện thông tin yêu cầu nghỉ phép không hợp lệ. 7.a.2. Hệ thống thông báo lỗi và nguyên nhân. 7.a.3. Hệ thống không tạo yêu cầu nghỉ phép. 7.a.4. Nhân viên thực hiện lại thao tác. **7.b. Nhân viên đã có yêu cầu nghỉ phép cho ca làm việc** 7.b.1. Hệ thống phát hiện ca làm việc đã có yêu cầu nghỉ phép. 7.b.2. Hệ thống thông báo "Ca làm việc này đã có yêu cầu nghỉ phép". 

7.b.3. Hệ thống không tạo thêm yêu cầu nghỉ phép. 

|Tên Usecase|**Đăng nhập**|
|---|---|
|Tác nhân chính|Nhân viên ,quản lý|



|Mức|2|
|---|---|
|Người chịu trách|Hệ thống|
|nhiệm||
|Tiền điều kiện|Thiết bị phải kết nối mạng|
|Đảm bảo tối thiểu|Hệ thống không thay đổi trạng thái thông báo nếu thao<br>tác thất bại.|
|Đảm bảo thành công|Người dùngcó thể đăngnhậpvào hệ thống|
|Kích hoạt|Người dùngđăngnhập|



Chuỗi sự kiện chính: 

1. Người dùng nhập số điện thoại và mật khẩu. 

2. Người dùng chọn chức năng “Đăng nhập”. 3. Hệ thống kiểm tra và xác thực thông tin đăng nhập. 4. Hệ thống xác định vai trò của người dùng. 

5. Hệ thống chuyển người dùng đến giao diện tương ứng với vai trò đã <u>lựa chọn.</u> 

Luồng thay thế : 

# **4.a. Người dùng lựa chọn vai trò “Quản lý”** 

4.a.1. Hệ thống chuyển người dùng đến trang quản lý. 

# **4.b. Người dùng lựa chọn vai trò “Nhân viên”** 

4.b.1. Hệ thống kiểm tra thông tin tham gia của nhân viên. 

# **4.b.1.a. Nhân viên đã tham gia doanh nghiệp** 

4.b.1.a.1. Hệ thống hiển thị danh sách các doanh nghiệp mà nhân viên đã tham gia. 

4.b.1.a.2. Người dùng lựa chọn doanh nghiệp muốn truy cập. 

4.b.1.a.3. Hệ thống chuyển người dùng đến giao diện nhân viên của doanh nghiệp được lựa chọn. 

# **4.b.1.b. Nhân viên chưa tham gia doanh nghiệp** 

4.b.1.b.1. Hệ thống hiển thị chức năng nhập mã mời. 

4.b.1.b.2. Người dùng nhập mã mời do người quản lý cung cấp. 

4.b.1.b.3. Hệ thống kiểm tra mã mời. 4.b.1.b.4. Nếu mã mời hợp lệ, hệ thống tạo yêu cầu tham gia và gửi đến người quản lý tương ứng. 4.b.1.b.5. Hệ thống thông báo yêu cầu tham gia đã được gửi và đang chờ người quản lý phê duyệt. 

**3.a. Thông tin đăng nhập không hợp lệ** 

3.a.1. Hệ thống thông báo thông tin đăng nhập không chính xác. 

3.a.2. Người dùng nhập lại thông tin đăng nhập. 

Tên Usecase **Đăng ký** Tác nhân chính Nhân viên ,quản lý 

|Mức|2|
|---|---|
|Người chịu trách|Hệ thống|
|nhiệm||
|Tiền điều kiện|Thiết bị phải kết nối mạng<br>Số điện thoại chưa được đăng ký trong hệ thống|
|Đảm bảo tối thiểu|<br>Hệ thống không thay đổi trạng thái ,thông báo nếu thao<br>tác thất bại.|
|Đảm bảo thành công|Người dùngcó thể tạo tài khoản thành công|
|Kích hoạt|Người dùngđăngký|



Chuỗi sự kiện chính: 

1. Người dùng nhập số điện thoại và mật khẩu. 

2. Người dùng chọn chức năng “Đăng ký”. 

3. Hệ thống kiểm tra số điện thoại. 

4. Hệ thống gửi mã OTP đến số điện thoại người dùng và yêu cầu xác nhận trong thời gian quy định. 

5. Người dùng nhập mã OTP. 

6. Hệ thống xác thực mã OTP. 

7. Hệ thống tạo tài khoản mới. 

8. <u>Hệ thống chuyển người dùng đến trang đăng nhập</u> 

Luồng thay thế : 

# **3.a. Số điện thoại đã tồn tại** 

3.a.1. Hệ thống thông báo số điện thoại đã được đăng ký. 

3.a.2. Người dùng nhập số điện thoại khác. 

# **6.a. Mã OTP không hợp lệ hoặc hết thời gian** 

6.a.1. Hệ thống thông báo mã OTP không hợp lệ hoặc đã hết hạn. 

6.a.2. Người dùng nhập lại mã OTP hoặc yêu cầu gửi lại mã OTP. 

|Tên Usecase|**Phê duyệt yêu cầu thamgia**|
|---|---|
|Tác nhân chính|Quản lý|



|Mức|2|
|---|---|
|Người chịu trách<br>nhiệm|Hệ thống<br>|
|Tiền điều kiện|Thiết bị phải kết nối mạng<br>Quản lý đã đăng nhập vào hệ thống|
|Đảm bảo tối thiểu|Hệ thống không thay đổi quan hệ giữa nhân viên và<br>quản lý nếu yêu cầu chưa được phê duyệt|
|Đảm bảo thành công|Yêu cầu tham gia được xử lý và quan hệ giữa quản lý<br>và nhân viên được cập nhật nếu yêu cầu được phê<br>duyệt.|
|Kích hoạt|Quản lýchọn chức năng“Yêu cầu thamgia”|



Chuỗi sự kiện chính: 

1. Quản lý mở danh sách yêu cầu tham gia. 

2. Hệ thống hiển thị các yêu cầu đang chờ xử lý. 

3. Quản lý chọn một yêu cầu tham gia. 

4. Hệ thống hiển thị thông tin của nhân viên gửi yêu cầu. 

5. Quản lý chọn “Phê duyệt”. 

6. Hệ thống cập nhật trạng thái yêu cầu. 

7. Hệ thống tạo quan hệ giữa quản lý và nhân viên. 

8. <u>Hệ thống thông báo phê duyệt thành công.</u> 

Luồng thay thế : 

- **5.a. Quản lý từ chối yêu cầu** 

- 5.a.1. Quản lý chọn “Từ chối”. 

5.a.2. Hệ thống cập nhật trạng thái yêu cầu thành “Đã từ chối”. 

5.a.3. Hệ thống thông báo từ chối yêu cầu thành công. **2.a. Không có yêu cầu tham gia** 2.a.1. Hệ thống thông báo không có yêu cầu tham gia đang chờ xử lý. 

