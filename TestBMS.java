public class TestBMS {
    public static void main(String[] args) {
        CustomerRepository customerRepo = new CustomerRepository();

        // 1. Create a mock customer object using Phase 2 class
        Customer newCustomer = new Customer("John", "Doe", "john.doe@email.com", "supersecretpasswordhash");

        System.out.println("Attempting to save John Doe to the database...");
        
        // 2. Call the repository to save it to Docker
        boolean isSaved = customerRepo.saveCustomer(newCustomer);

        if (isSaved) {
            System.out.println("🎉 Success! John Doe was written to the PostgreSQL database.");
            
            // 3. Try to fetch the customer back out to prove it's there
            System.out.println("\nTesting database retrieval...");
            Customer retrieved = customerRepo.getCustomerByEmail("john.doe@email.com");
            if (retrieved != null) {
                System.out.println("🎉 Retrieved Successfully! Welcome back, " + retrieved.getFirstName() + " " + retrieved.getLastName());
            }
        } else {
            System.out.println("❌ Test Failed.");
        }
    }
}


