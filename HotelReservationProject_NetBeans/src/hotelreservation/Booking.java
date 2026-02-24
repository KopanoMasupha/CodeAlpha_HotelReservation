package hotelreservation;

class Booking {
    String id, name, surname, hotel, roomType;
    int days, adults, children;
    double total;
    boolean paid;

    Booking(String id, String name, String surname, String hotel,
            int days, int adults, int children, String roomType,
            double total, boolean paid) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.hotel = hotel;
        this.days = days;
        this.adults = adults;
        this.children = children;
        this.roomType = roomType;
        this.total = total;
        this.paid = paid;
    }
}
