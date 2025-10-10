package polysphere.dms.com.dtts.services;

public class EnrollmentManager {
    public interface Listener {
        void onBioVerified(BioResult result);
    }

    public static class BioResult {
        public String section;
        public String uuid;
        public boolean biometric_password;
        // other fields
    }

    // Notify listeners when fingerprint compare passes, etc.
}
