public class Main {
    public static void main(String[] args) throws Exception {
        var r = new org.springframework.core.io.ClassPathResource("words.txt");
        System.out.println("exists=" + r.exists());
    }
}
