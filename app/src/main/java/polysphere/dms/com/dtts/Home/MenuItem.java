package polysphere.dms.com.dtts.Home;

public class MenuItem {
    private final int iconRes;
    private final String label;
    private final Class<?> destination; // activity to open

    public MenuItem(int iconRes, String label, Class<?> destination) {
        this.iconRes = iconRes;
        this.label = label;
        this.destination = destination;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getLabel() {
        return label;
    }

    public Class<?> getDestination() {
        return destination;
    }
}
