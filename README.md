# Project-final-ArtShopWeb

Final Project_WebProgramming

Src code web bán tranh + database
 
Chạy file sql trước để tạo local database.

Trong file src/main/java/com.artshopweb/utils/ConstantUtils.
    Thay đổi tên server name, user name và database password theo cài đặt trên SQL để có thể kết nối vào database

 public interface ConstantUtils {

   int DB_PORT = 3306;

   String SERVER_NAME = "...";  //localhost name

   String DB_NAME = "artshopdb"; // tên database

   String DB_USERNAME = "root"; // tên username

   String DB_PASSWORD = "0345762405"; // mật khẩu

   String IMAGE_PATH = "C:\\var\\webapp\\images\\"; // đây là đường dẫn cho folder hình ảnh sản phẩm(imgae.rar) dùng trong project phải tạo folder 
}
