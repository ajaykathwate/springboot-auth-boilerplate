# Notification Service Documentation

A comprehensive multi-channel notification service supporting Email, SMS, WhatsApp, Push Notifications, and In-App notifications with templating, rate limiting, and retry mechanisms.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Supported Channels](#supported-channels)
- [Quick Start](#quick-start)
- [Using the NotificationService](#using-the-notificationservice)
- [Adding New Notifications](#adding-new-notifications)
- [Template System](#template-system)
- [Rate Limiting](#rate-limiting)
- [Retry Mechanism](#retry-mechanism)
- [Configuration Reference](#configuration-reference)

---

## Overview

The Notification Service provides a unified API for sending notifications across multiple channels. It uses:

- **RabbitMQ** for asynchronous message processing
- **Redis** for rate limiting and caching
- **Thymeleaf** for HTML email templates
- **Dead Letter Queue (DLQ)** for failed message handling

### Key Features

- Multi-channel support (Email, SMS, WhatsApp, Push, In-App)
- Template-based content rendering
- Per-channel rate limiting
- Exponential backoff retry mechanism
- Fire-and-forget pattern for non-blocking sends
- Notification history and status tracking

---

## Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Your Service   │────▶│ NotificationService│────▶│    RabbitMQ     │
└─────────────────┘     └──────────────────┘     └────────┬────────┘
                                                          │
                        ┌─────────────────────────────────┼─────────────────────────────────┐
                        │                                 │                                 │
                        ▼                                 ▼                                 ▼
               ┌────────────────┐               ┌────────────────┐               ┌────────────────┐
               │  Email Worker  │               │   SMS Worker   │               │  Push Worker   │
               └───────┬────────┘               └───────┬────────┘               └───────┬────────┘
                       │                                │                                │
                       ▼                                ▼                                ▼
               ┌────────────────┐               ┌────────────────┐               ┌────────────────┐
               │  SMTP/Gmail    │               │     Twilio     │               │    Firebase    │
               └────────────────┘               └────────────────┘               └────────────────┘
```

---

## Supported Channels

| Channel   | Provider           | Status    |
|-----------|--------------------|-----------|
| EMAIL     | JavaMail (Gmail)   | Ready     |
| SMS       | Twilio             | Ready     |
| WHATSAPP  | Twilio             | Ready     |
| PUSH      | Firebase FCM       | Ready     |
| IN_APP    | Database storage   | Ready     |

---

## Quick Start

### 1. Inject the NotificationService

```java
@Service
@RequiredArgsConstructor
public class YourService {

    private final NotificationService notificationService;
}
```

### 2. Send a Simple Email

```java
Map<String, Object> data = Map.of(
    "userName", "John Doe",
    "orderNumber", "ORD-12345"
);

notificationService.sendEmail(
    userId,           // Long - user ID (can be null for unauthenticated)
    "user@example.com",
    "order-confirmation",  // template code
    "Your Order Confirmation",  // subject
    data
);
```

### 3. Send Multi-Channel Notification

```java
NotificationRequest request = NotificationRequest.builder()
    .channels(Set.of(NotificationChannel.EMAIL, NotificationChannel.PUSH))
    .userId(userId)
    .recipient(RecipientDetails.builder()
        .email("user@example.com")
        .fcmToken("firebase-token-here")
        .build())
    .templateCode("welcome")
    .templateData(Map.of("userName", "John"))
    .subject("Welcome!")
    .title("Welcome to Our App")
    .build();

List<Long> notificationIds = notificationService.send(request);
```

---

## Using the NotificationService

### Available Methods

#### Single Channel Methods (Convenience)

```java
// Email
Long sendEmail(Long userId, String email, String templateCode, Map<String, Object> templateData);
Long sendEmail(Long userId, String email, String templateCode, String subject, Map<String, Object> templateData);

// SMS
Long sendSms(Long userId, String phone, String templateCode, Map<String, Object> templateData);

// WhatsApp
Long sendWhatsApp(Long userId, String phone, String templateCode, Map<String, Object> templateData);

// Push Notification
Long sendPush(Long userId, String fcmToken, String templateCode, String title, Map<String, Object> templateData);

// In-App Notification
Long sendInApp(Long userId, String templateCode, Map<String, Object> templateData);
```

#### Multi-Channel Method

```java
List<Long> send(NotificationRequest request);
```

#### Query Methods

```java
Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);
Page<NotificationResponse> getUserNotifications(Long userId, NotificationChannel channel, Pageable pageable);
Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable);
NotificationResponse getNotification(Long notificationId, Long userId);
long getUnreadCount(Long userId);
```

#### Status Management

```java
boolean markAsRead(Long notificationId, Long userId);
int markAllAsRead(Long userId);
```

---

## Adding New Notifications

### Step 1: Create a Template

Create an HTML template file in the appropriate channel folder:

```
src/main/resources/templates/notifications/
├── email/
│   └── your-template.html
├── sms/
│   └── your-template.txt
├── whatsapp/
│   └── your-template.txt
├── push/
│   └── your-template.txt
└── inapp/
    └── your-template.txt
```

**Example: `email/payment-success.html`**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Payment Successful</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .header { background: #059669; color: white; padding: 20px; text-align: center; }
        .content { padding: 30px; background: #f9fafb; }
        .amount { font-size: 24px; font-weight: bold; color: #059669; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Payment Successful</h1>
    </div>
    <div class="content">
        <p>Hi <span th:text="${userName}">User</span>,</p>
        <p>Your payment of <span class="amount" th:text="${amount}">$0.00</span> was successful.</p>
        <p>Transaction ID: <span th:text="${transactionId}">TXN-000</span></p>
        <p>Thank you for your purchase!</p>
    </div>
</body>
</html>
```

**Example: `sms/payment-success.txt`**

```
Your payment of [[${amount}]] was successful. Transaction ID: [[${transactionId}]]
```

### Step 2: Use the Template in Your Service

```java
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final NotificationService notificationService;

    public void processPayment(Payment payment, User user) {
        // ... payment processing logic ...

        // Send notification
        Map<String, Object> data = Map.of(
            "userName", user.getFirstName(),
            "amount", formatCurrency(payment.getAmount()),
            "transactionId", payment.getTransactionId()
        );

        notificationService.sendEmail(
            user.getId(),
            user.getEmail(),
            "payment-success",  // template code (filename without extension)
            "Payment Confirmation",
            data
        );
    }
}
```

### Step 3: Multi-Channel Notification Example

```java
public void sendOrderConfirmation(Order order, User user) {
    Map<String, Object> data = Map.of(
        "userName", user.getFirstName(),
        "orderNumber", order.getOrderNumber(),
        "items", order.getItems(),
        "total", order.getTotal()
    );

    NotificationRequest request = NotificationRequest.builder()
        .channels(Set.of(
            NotificationChannel.EMAIL,
            NotificationChannel.SMS,
            NotificationChannel.PUSH
        ))
        .userId(user.getId())
        .recipient(RecipientDetails.builder()
            .email(user.getEmail())
            .phone(user.getPhoneNumber())
            .fcmToken(user.getFcmToken())
            .build())
        .templateCode("order-confirmation")
        .templateData(data)
        .subject("Order #" + order.getOrderNumber() + " Confirmed")
        .title("Order Confirmed!")
        .build();

    notificationService.send(request);
}
```

---

## Template System

### Template Variables

Use Thymeleaf syntax in templates:

| Syntax | Description |
|--------|-------------|
| `${variable}` | Variable substitution |
| `th:text="${var}"` | Text content (HTML escaped) |
| `th:utext="${var}"` | Unescaped HTML |
| `th:each="item : ${items}"` | Loop iteration |
| `th:if="${condition}"` | Conditional rendering |

### Template Location

Templates are loaded from:
```
classpath:/templates/notifications/{channel}/{templateCode}.{extension}
```

| Channel  | Extension |
|----------|-----------|
| email    | .html     |
| sms      | .txt      |
| whatsapp | .txt      |
| push     | .txt      |
| inapp    | .txt      |

---

## Rate Limiting

Rate limiting is configured per channel in `application.yaml`:

```yaml
notification:
  rate-limit:
    email:
      max-requests: 50
      window-seconds: 3600  # 50 emails per hour
    sms:
      max-requests: 10
      window-seconds: 3600  # 10 SMS per hour
    whatsapp:
      max-requests: 20
      window-seconds: 3600
    push:
      max-requests: 100
      window-seconds: 3600
    in_app:
      max-requests: 200
      window-seconds: 3600
```

Rate limits are applied per-user. If a user exceeds the limit, notifications are queued and processed later.

---

## Retry Mechanism

Failed notifications are automatically retried with exponential backoff:

```yaml
notification:
  retry:
    max-attempts: 10
    initial-backoff-ms: 1000      # 1 second
    multiplier: 2.0               # Double each retry
    max-backoff-ms: 3600000       # Max 1 hour between retries
```

**Retry Schedule Example:**
- Attempt 1: Immediate
- Attempt 2: 1 second later
- Attempt 3: 2 seconds later
- Attempt 4: 4 seconds later
- ... (continues with exponential backoff)
- After 10 failures: Moved to Dead Letter Queue (DLQ)

---

## Configuration Reference

### Full Configuration

```yaml
notification:
  # Rate limiting
  rate-limit:
    email:
      max-requests: 50
      window-seconds: 3600
    sms:
      max-requests: 10
      window-seconds: 3600
    whatsapp:
      max-requests: 20
      window-seconds: 3600
    push:
      max-requests: 100
      window-seconds: 3600
    in_app:
      max-requests: 200
      window-seconds: 3600

  # Retry configuration
  retry:
    max-attempts: 10
    initial-backoff-ms: 1000
    multiplier: 2.0
    max-backoff-ms: 3600000

  # Queue configuration
  queue:
    exchange: notification.exchange
    dlx-exchange: notification.dlx
    dlq-queue: notification.dlq
    prefetch-count: 1

  # Template configuration
  template:
    base-path: classpath:/templates/notifications/
    cache-enabled: true

  # Twilio (SMS & WhatsApp)
  twilio:
    enabled: false
    account-sid: ${TWILIO_ACCOUNT_SID:}
    auth-token: ${TWILIO_AUTH_TOKEN:}
    from-number: ${TWILIO_FROM_NUMBER:}
    whatsapp-number: ${TWILIO_WHATSAPP_NUMBER:}

  # Firebase (Push)
  firebase:
    enabled: false
    service-account-path: ${FIREBASE_SERVICE_ACCOUNT_PATH:}
```

### Environment Variables

| Variable | Description |
|----------|-------------|
| `TWILIO_ENABLED` | Enable/disable Twilio (true/false) |
| `TWILIO_ACCOUNT_SID` | Twilio Account SID |
| `TWILIO_AUTH_TOKEN` | Twilio Auth Token |
| `TWILIO_FROM_NUMBER` | Twilio phone number for SMS |
| `TWILIO_WHATSAPP_NUMBER` | Twilio WhatsApp number |
| `FIREBASE_ENABLED` | Enable/disable Firebase (true/false) |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | Path to Firebase service account JSON |

---

## Common Use Cases

### 1. User Registration Welcome Email

```java
public void sendWelcomeEmail(User user) {
    notificationService.sendEmail(
        user.getId(),
        user.getEmail(),
        "welcome",
        "Welcome to Our Platform!",
        Map.of("userName", user.getFirstName())
    );
}
```

### 2. Password Reset

```java
public void sendPasswordResetEmail(User user, String resetToken) {
    notificationService.sendEmail(
        user.getId(),
        user.getEmail(),
        "password-reset",
        "Reset Your Password",
        Map.of(
            "userName", user.getFirstName(),
            "resetLink", buildResetLink(resetToken),
            "expiryMinutes", 30
        )
    );
}
```

### 3. Order Status Update (Multi-Channel)

```java
public void notifyOrderShipped(Order order, User user) {
    Map<String, Object> data = Map.of(
        "orderNumber", order.getOrderNumber(),
        "trackingNumber", order.getTrackingNumber(),
        "estimatedDelivery", order.getEstimatedDelivery()
    );

    NotificationRequest request = NotificationRequest.builder()
        .channels(Set.of(NotificationChannel.EMAIL, NotificationChannel.PUSH))
        .userId(user.getId())
        .recipient(RecipientDetails.builder()
            .email(user.getEmail())
            .fcmToken(user.getFcmToken())
            .build())
        .templateCode("order-shipped")
        .templateData(data)
        .subject("Your Order Has Shipped!")
        .title("Order Shipped")
        .build();

    notificationService.send(request);
}
```

---

## Troubleshooting

### Common Issues

1. **Template not found**: Ensure template file exists at the correct path with correct extension
2. **Rate limit exceeded**: Check rate limit configuration, consider increasing limits
3. **Email not sending**: Verify SMTP credentials and Gmail app password
4. **Push not working**: Ensure Firebase is enabled and service account path is correct

### Monitoring

- Check RabbitMQ Management UI at `http://localhost:15672` (guest/guest)
- Monitor Dead Letter Queue for failed notifications
- Check application logs for detailed error messages

---

## Best Practices

1. **Always use templates** - Avoid hardcoding notification content
2. **Include unsubscribe links** - For marketing emails
3. **Test templates** - Preview rendered templates before deployment
4. **Monitor rate limits** - Adjust based on actual usage patterns
5. **Handle failures gracefully** - Design for eventual delivery, not immediate
