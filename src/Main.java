import model.Account;
import model.Customer;
import repository.*;

public class Main {
    public static void main(String[] args) {
        CustomerRepository customerRepo = new CustomerRepository();

        // 1. Create a mock customer object using Phase 2 class
        Customer newCustomer = new Customer("Mayank", "Anand", "anand.mayank@email.com", "superstar002");

        System.out.println("Attempting to save Mayank Anand to the database...");

        // 2. Call the repository to save it to Docker
        boolean isSaved = customerRepo.saveCustomer(newCustomer);

        if (isSaved) {
            System.out.println("🎉 Success! Mayank Anand was written to the PostgreSQL database.");

            // 3. Try to fetch the customer back out to prove it's there
            System.out.println("\nTesting database retrieval...");
            Customer retrieved = customerRepo.getCustomerByEmail("anand.mayank@email.com");
            if (retrieved != null) {
                System.out.println("🎉 Retrieved Successfully! Welcome back, " + retrieved.getFirstName() + " "
                        + retrieved.getLastName());
            }
        } else {
            System.out.println("❌ Test Failed.");
        }
    }
}
