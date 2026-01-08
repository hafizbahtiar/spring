# Stripe Webhook Setup Guide

## Overview

The Stripe webhook secret (`whsec_xxx`) is used to verify that webhook requests are actually coming from Stripe. This is a critical security feature to prevent unauthorized requests.

## How to Get the Webhook Secret

### Option 1: Stripe Dashboard (Recommended for Production/Testing)

1. **Log in to Stripe Dashboard**

   - Go to https://dashboard.stripe.com/
   - Make sure you're in **Test mode** (toggle in top right) for testing, or **Live mode** for production

2. **Navigate to Webhooks**

   - Click **Developers** in the left sidebar
   - Click **Webhooks**

3. **Create or Select Webhook Endpoint**

   - Click **Add endpoint** (or select an existing one)
   - Enter your endpoint URL:
     - **Production**: `https://your-domain.com/api/v1/webhooks/stripe`
     - **Local Testing**: Use Stripe CLI (see Option 2 below)

4. **Select Events to Listen To**

   - `payment_intent.succeeded` - Payment completed successfully
   - `payment_intent.payment_failed` - Payment failed
   - `payment_intent.canceled` - Payment canceled
   - `charge.refunded` - Refund processed (optional)

5. **Get the Signing Secret**
   - After creating the endpoint, click on it
   - Click **Signing secret** → **Reveal**
   - Copy the secret (it starts with `whsec_`)

### Option 2: Stripe CLI (Recommended for Local Development)

1. **Install Stripe CLI**

   ```bash
   # macOS
   brew install stripe/stripe-cli/stripe

   # Windows (using Scoop)
   scoop install stripe

   # Linux
   # Download from https://github.com/stripe/stripe-cli/releases
   ```

2. **Login to Stripe**

   ```bash
   stripe login
   ```

   This will open your browser to authenticate.

3. **Forward Webhooks to Local Server**

   ```bash
   stripe listen --forward-to localhost:8080/api/v1/webhooks/stripe
   ```

   The CLI will output something like:

   ```
   > Ready! Your webhook signing secret is whsec_xxxxxxxxxxxxx
   ```

4. **Copy the Webhook Secret**
   - Copy the `whsec_xxx` value shown in the terminal
   - Use this for local development

## Configuration

### Update application.properties

```properties
# Stripe Webhook Secret (for webhook signature verification)
stripe.webhook-secret=${STRIPE_WEBHOOK_SECRET:whsec_your-webhook-secret-here}
```

### Using Environment Variable (Recommended)

Set the environment variable:

```bash
export STRIPE_WEBHOOK_SECRET=whsec_your-actual-webhook-secret
```

Or in your `.env` file:

```
STRIPE_WEBHOOK_SECRET=whsec_your-actual-webhook-secret
```

## Testing Webhooks Locally

### Using Stripe CLI

1. **Start your Spring Boot application**

   ```bash
   ./mvnw spring-boot:run
   ```

2. **In another terminal, forward webhooks**

   ```bash
   stripe listen --forward-to localhost:8080/api/v1/webhooks/stripe
   ```

3. **Trigger test events**

   ```bash
   # Test payment succeeded
   stripe trigger payment_intent.succeeded

   # Test payment failed
   stripe trigger payment_intent.payment_failed
   ```

### Using ngrok (Alternative)

1. **Install ngrok**

   ```bash
   brew install ngrok  # macOS
   # or download from https://ngrok.com/
   ```

2. **Expose local server**

   ```bash
   ngrok http 8080
   ```

3. **Copy the HTTPS URL** (e.g., `https://abc123.ngrok.io`)

4. **Create webhook endpoint in Stripe Dashboard**
   - URL: `https://abc123.ngrok.io/api/v1/webhooks/stripe`
   - Copy the signing secret

## Security Notes

⚠️ **Important Security Considerations:**

1. **Never commit webhook secrets to version control**

   - Use environment variables or secure secret management
   - Add `application.properties` to `.gitignore` if it contains secrets (or use separate config files)

2. **Use different secrets for test and production**

   - Test mode: `whsec_test_xxx`
   - Live mode: `whsec_live_xxx`

3. **Rotate secrets if compromised**

   - In Stripe Dashboard → Webhooks → Endpoint → Signing secret → Rotate

4. **Verify signatures in production**
   - Always verify webhook signatures (already implemented in `StripePaymentProvider.validateWebhookSignature()`)

## Troubleshooting

### "Webhook signature validation failed"

- Check that the webhook secret matches the one in Stripe Dashboard
- Ensure you're using the correct secret for test/live mode
- Verify the endpoint URL matches exactly

### "No webhook secret configured"

- Make sure `stripe.webhook-secret` is set in `application.properties`
- Or set `STRIPE_WEBHOOK_SECRET` environment variable

### Webhooks not reaching local server

- Ensure Stripe CLI is forwarding: `stripe listen --forward-to localhost:8080/api/v1/webhooks/stripe`
- Check firewall settings
- Verify the endpoint URL is correct

## Next Steps

After setting up webhooks:

1. Implement `StripeWebhookHandler` controller (see TODO.md)
2. Process webhook events to update payment status
3. Test with Stripe CLI or Dashboard

## References

- [Stripe Webhooks Documentation](https://stripe.com/docs/webhooks)
- [Stripe CLI Documentation](https://stripe.com/docs/stripe-cli)
- [Webhook Security Best Practices](https://stripe.com/docs/webhooks/signatures)
