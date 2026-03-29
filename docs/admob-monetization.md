# AdMob Monetization Guide

## 1. Create AdMob Setup

1. Create or sign in to AdMob account.
2. Add app (Android).
3. Create ad units:
   - Banner (recommended first)
   - Interstitial (optional, conservative usage)

## 2. Configure Android App

1. Add Google Mobile Ads SDK dependency.
2. Add AdMob App ID in `AndroidManifest.xml` metadata.
3. Use test ad unit IDs during development.

## 3. Integration Strategy

Recommended first version:

- Show one banner in search screen footer or detail screen footer.
- Do not block primary search flow.
- Avoid aggressive interstitial frequency.

## 4. Consent and Privacy

- Implement required consent flow by region.
- Provide privacy policy page and link in store listing.
- Declare ad usage in Play Console Data safety and Ads sections.

## 5. Policy Safety

- Never click own ads.
- Use test IDs for development builds.
- Avoid accidental click patterns (ads too close to navigation buttons).
- Keep content and ad presentation policy-compliant.

## 6. Production Rollout

1. Switch test ad unit IDs to production IDs.
2. Release to internal testing first.
3. Monitor fill rate, eCPM, and policy center.
4. Tune placement only after stable behavior is confirmed.
