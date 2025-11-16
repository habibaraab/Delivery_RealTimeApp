# 🚀 Real-Time Delivery Bidding Application (Spring Boot & WebSocket)

This project is a functional demonstration of a real-time delivery backend built with **Spring Boot**, focusing on instantaneous communication, secure session management, and a live bidding workflow where available drivers receive and respond to new orders in real time.

---

## ✨ Key Features Implemented

This application supports a full delivery lifecycle secured by **JWT** and **WebSocket** technologies.

| Feature Area              | Description                                                                 | Technology                                 |
|---------------------------|-----------------------------------------------------------------------------|---------------------------------------------|
| Real-Time Bidding         | Drivers instantly receive new orders via broadcast.                         | WebSocket (`/topic/drivers`)                |
| Secure Private Messaging  | Clients receive private bid notifications (submitted or accepted).          | WebSocket (`/user/queue/bids`)              |
| Live Driver Tracking      | Driver GPS (Lat/Lon) is streamed privately to the customer tracking channel.| WebSocket (`/user/queue/track/{id}`)        |
| JWT Security Bridge       | JWT tokens are validated and mapped to STOMP sessions.                      | Spring Security + AuthChannelInterceptor    |
| User Availability         | Driver availability is auto-updated based on WebSocket connection state.    | WebSocketEventListener                      |

---

## 💻 Technical Stack

- **Backend Framework:** Spring Boot 3.x  
- **Database:** MySQL / (or H2 for development)  
- **ORM:** Spring Data JPA  
- **Real-Time Protocol:** WebSocket (STOMP)  
- **Security:** Spring Security 6.x + JWT  
- **Mapping:** GPS coordinates (Latitude/Longitude)

---

## 📡 WebSocket Endpoints Summary

### **HTTP Endpoints**

| Path                   | Command | Purpose                                              | Secured By |
|------------------------|---------|------------------------------------------------------|------------|
| `/api/orders`          | POST    | Triggers broadcast when a new order is created.     | HTTP/JWT   |
| `/api/orders/accept-bid` | POST | Customer accepts a bid and finalizes assignment.     | HTTP/JWT   |

### **STOMP Send Endpoints**

| Path                      | Command | Purpose                                     | Secured By     |
|---------------------------|---------|---------------------------------------------|-----------------|
| `/app/bid/submit`         | SEND    | Driver submits a bid for an order.          | STOMP/Principal |
| `/app/track/update-location` | SEND | Driver sends real-time GPS location updates.| STOMP/Principal |

### **Subscription Channels (Server → Client)**

| Path                        | Receiver            | Usage                                             |
|----------------------------|---------------------|---------------------------------------------------|
| `/topic/drivers/new-orders` | All Available Drivers | Receive new orders instantly.                    |
| `/user/queue/bids`         | Specific Client     | Private bid notifications.                        |
| `/user/queue/track/{id}`   | Specific Client     | Live driver GPS tracking data.                    |

---

## ⚙️ Project Setup & Testing

### **Prerequisites**
- Java 21+
- Maven 
- Database configured in `application.properties`

---

### **Testing the Real-Time Flow**

#### ✔ 1. Authentication  
Obtain JWT tokens for both **Client** and **Driver** using:  
Order Creation (HTTP)
➜ Bid Broadcast (WebSocket)

Bid Submission (STOMP SEND)
➜ Private Notification (Client)

Live Tracking (STOMP SEND)
➜ Real-Time Map Update (Client)


---

## 📦 Status
This project demonstrates **real-time delivery logic**, full WebSocket–JWT bridging, and a working end-to-end bidding workflow suitable for production scaling.

---

## 📜 License
This project can be used, modified, and extended freely.

