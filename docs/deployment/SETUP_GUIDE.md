# CI/CD Setup Guide - Complete Configuration

> Step-by-step guide to configure GitHub Actions CI/CD pipeline with Slack notifications

## 📋 Table of Contents

1. [Configure GitHub Secrets](#1-configure-github-secrets)
2. [Set up Production Environment Approval](#2-set-up-production-environment-approval)
3. [Test Complete Pipeline](#3-test-complete-pipeline)
4. [Add Slack Notifications](#4-add-slack-notifications)
5. [Enable Dependabot](#5-enable-dependabot)
6. [Verification Checklist](#6-verification-checklist)

---

## 1. 📐 Configure GitHub Secrets

### Backend Secrets (portfolio-spring)

**Navigate to Repository Settings:**
```
https://github.com/BernardUriza/portfolio-spring/settings/secrets/actions
```

**Or manually:**
1. Go to repository → **Settings** (⚙️ tab)
2. Left sidebar → **Secrets and variables** → **Actions**
3. Click **"New repository secret"**

**Add these secrets:**

#### Secret 1: RENDER_STAGING_DEPLOY_HOOK

```
Name: RENDER_STAGING_DEPLOY_HOOK
Value: https://api.render.com/deploy/srv-XXXXX?key=YYYYY
```

**How to get Render Deploy Hook:**
1. Go to [Render Dashboard](https://dashboard.render.com/)
2. Select your **staging** service
3. Click **"Settings"** tab
4. Scroll to **"Deploy Hook"** section
5. Click **"Create Deploy Hook"**
6. Copy the generated URL
7. Paste as secret value in GitHub

#### Secret 2: RENDER_PROD_DEPLOY_HOOK

```
Name: RENDER_PROD_DEPLOY_HOOK
Value: https://api.render.com/deploy/srv-ZZZZZ?key=AAAAA
```

(Same process but for **production** service)

#### Secret 3: SLACK_WEBHOOK_URL (Optional)

```
Name: SLACK_WEBHOOK_URL
Value: https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXX
```

(See [Section 4](#4-add-slack-notifications) for setup)

---

### Frontend Secrets (portfolio-frontend)

**Navigate to:**
```
https://github.com/BernardUriza/portfolio-frontend/settings/secrets/actions
```

**Add these secrets:**

#### For Netlify:

**Secret 1: STAGING_DEPLOY_TOKEN**
```
Name: STAGING_DEPLOY_TOKEN
Value: [Your Netlify Token]
```

**How to get Netlify Token:**
1. Go to [Netlify User Applications](https://app.netlify.com/user/applications)
2. Click **"New access token"**
3. Name: "GitHub Actions Staging"
4. Click **"Generate token"**
5. Copy and save immediately (shown only once)

**Secret 2: PROD_DEPLOY_TOKEN**
```
Name: PROD_DEPLOY_TOKEN
Value: [Your Netlify Token]
```

(Can use same token or create separate)

#### For Vercel (Alternative):

```
Name: VERCEL_TOKEN
Value: [Your Vercel Token]
```

**Get Vercel Token:**
1. Go to [Vercel Tokens](https://vercel.com/account/tokens)
2. Click **"Create"**
3. Name: "GitHub Actions"
4. Scope: Full Account
5. Copy token

**Secret 3: SLACK_WEBHOOK_URL**
```
Same as backend
```

---

## 2. 🔒 Set up Production Environment Approval

### Backend (portfolio-spring)

**Step 1: Navigate to Environments**
```
https://github.com/BernardUriza/portfolio-spring/settings/environments
```

Or: **Settings** → **Environments** (left sidebar)

**Step 2: Create Production Environment**

1. Click **"New environment"** (green button)
2. Enter name: `production`
3. Click **"Configure environment"**

**Step 3: Configure Protection Rules**

**✅ Required Reviewers:**
- Check ☑️ **"Required reviewers"**
- Click search field
- Select yourself: **@BernardUriza**
- You can add up to 6 reviewers
- At least 1 must approve before deployment

**⏱️ Wait Timer (Optional):**
- Check ☑️ **"Wait timer"**
- Enter minutes (e.g., `5`)
- Adds cooldown before deployment starts
- Useful for production safety

**🌿 Deployment Branches:**
- Check ☑️ **"Deployment branches"**
- Select **"Selected branches"**
- Click **"Add deployment branch rule"**
- Enter pattern: `main` or `prod`
- This restricts which branches can deploy to production

**Step 4: Environment Secrets (Optional)**

If you need production-specific secrets:

1. Scroll to **"Environment secrets"**
2. Click **"Add secret"**
3. Example secrets:
   ```
   DATABASE_URL_PROD
   API_KEY_PROD
   ```

These override repository secrets for production deployments only.

**Step 5: Environment Variables (Optional)**

1. Scroll to **"Environment variables"**
2. Click **"Add variable"**
3. Example:
   ```
   Name: ENVIRONMENT
   Value: production

   Name: LOG_LEVEL
   Value: error
   ```

**Step 6: Save**

Click **"Save protection rules"** at the bottom.

---

### Frontend (portfolio-frontend)

**Repeat the same steps:**
```
https://github.com/BernardUriza/portfolio-frontend/settings/environments
```

1. Create environment: `production`
2. Add required reviewer: @BernardUriza
3. Set deployment branches: `main` or `prod`
4. Save protection rules

---

## 3. 🧪 Test Complete Pipeline

### Option A: Test with Develop Branch

#### Backend Test

```bash
# Navigate to backend
cd /Users/bernardurizaorozco/Documents/portfolio-spring

# Create develop branch if it doesn't exist
git checkout -b develop main

# Make a small test change
echo "\n# CI/CD Pipeline Test - $(date)" >> README.md

# Commit and push
git add README.md
git commit -m "test: Trigger staging deployment pipeline"
git push origin develop
```

**Expected Result:**
1. GitHub Actions workflow triggers automatically
2. All jobs run: tests → build → quality → security
3. `deploy-staging` job executes
4. Render receives webhook and deploys to staging
5. Slack notification sent (if configured)

**View Progress:**
```
https://github.com/BernardUriza/portfolio-spring/actions
```

---

#### Frontend Test

```bash
# Navigate to frontend
cd /Users/bernardurizaorozco/Documents/portfolio-frontend

# Create develop branch
git checkout -b develop prod

# Make test change
echo "\n# CI/CD Pipeline Test - $(date)" >> README.md

# Commit and push
git add README.md
git commit -m "test: Trigger staging deployment pipeline"
git push origin develop
```

**Expected Result:**
1. Build & test (Node 18.x + 20.x)
2. E2E tests with Playwright
3. Code quality scan
4. Staging deployment
5. Slack notification

---

### Option B: Test Production Deployment with Approval

#### Backend Production Deploy

```bash
cd /Users/bernardurizaorozco/Documents/portfolio-spring

# Ensure you're on main branch
git checkout main

# Merge develop (or make a test change)
git merge develop

# Push to main
git push origin main
```

**Expected Result:**

1. **Pipeline Triggers**: All jobs start running
2. **Tests Complete**: ~3-5 minutes
3. **Build Succeeds**: JAR artifact created
4. **Quality & Security Pass**
5. **Production Job Pauses**: ⏸️ Waiting for approval

**Approval Process:**

1. **Email Notification**: You'll receive an email
   ```
   Subject: Review required for deployment to production
   From: notifications@github.com
   ```

2. **Go to Actions Tab**:
   ```
   https://github.com/BernardUriza/portfolio-spring/actions
   ```

3. **Click on the running workflow**

4. **You'll see a yellow banner**:
   ```
   ⚠️ deploy-production › production
   Review required before deploying to production
   ```

5. **Click "Review deployments"** button

6. **Review Dialog Opens**:
   - ☑️ Check **"production"**
   - (Optional) Add comment: "Approved for deployment"
   - Click **"Approve and deploy"**

7. **Deployment Continues**:
   - Render receives webhook
   - App deploys to production
   - Slack notification sent
   - Workflow completes ✅

---

## 4. 🔔 Add Slack Notifications

### Step 1: Create Slack App

1. **Go to Slack API**:
   ```
   https://api.slack.com/apps
   ```

2. **Click "Create New App"**

3. **Select "From scratch"**

4. **Fill in details**:
   ```
   App Name: GitHub Actions Notifier
   Workspace: [Your Workspace]
   ```

5. **Click "Create App"**

---

### Step 2: Enable Incoming Webhooks

1. **In app settings, click "Incoming Webhooks"** (left sidebar)

2. **Toggle ON** to activate

3. **Click "Add New Webhook to Workspace"**

4. **Select Channel**:
   - Choose where to post (e.g., `#deployments`, `#ci-cd`)
   - Click **"Allow"**

5. **Copy Webhook URL**:
   ```
   https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXX
   ```

   ⚠️ **Save this immediately** - you'll need it for GitHub

---

### Step 3: Add Webhook to GitHub Secrets

**Backend:**
```
https://github.com/BernardUriza/portfolio-spring/settings/secrets/actions
```

**Frontend:**
```
https://github.com/BernardUriza/portfolio-frontend/settings/secrets/actions
```

**Create secret:**
```
Name: SLACK_WEBHOOK_URL
Value: https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXX
```

---

### Step 4: Test Slack Notifications

**Manual Test (Optional):**

```bash
# Test the webhook directly
curl -X POST https://hooks.slack.com/services/YOUR/WEBHOOK/URL \
  -H 'Content-Type: application/json' \
  -d '{
    "text": "🧪 Test notification from CI/CD setup",
    "blocks": [
      {
        "type": "section",
        "text": {
          "type": "mrkdwn",
          "text": "Testing Slack integration for GitHub Actions"
        }
      }
    ]
  }'
```

You should see a message in your Slack channel immediately.

**Production Test:**

Push to `develop` branch and verify Slack receives deployment notifications.

---

### Notification Examples

**Staging Deployment (Success):**
```
✅ Backend Staging Deployment Successful

Repository: portfolio-spring
Environment: Staging
Branch: develop
Author: BernardUriza
```

**Production Deployment (Success):**
```
🚀 Production Deployment Complete

Repository: portfolio-spring
Branch: main
Commit: a1f1ac7
Author: BernardUriza

🔗 View Production Site
```

**Deployment Failure:**
```
❌ Backend Production Deployment Failed!

Repository: portfolio-spring
Branch: main

🔍 View Logs
```

---

### Email Notifications (Alternative)

If you don't want Slack, GitHub sends email notifications automatically for:
- ✅ Workflow successes
- ❌ Workflow failures
- ⏸️ Approval required for protected environments

**Configure email preferences:**
```
https://github.com/settings/notifications
```

Check ☑️:
- "Actions" notifications
- "Deployments" notifications

---

## 5. ✅ Enable Dependabot

Dependabot is already configured via `.github/dependabot.yml`, but you need to enable alerts.

### Step 1: Enable Dependabot Alerts

**Backend:**
```
https://github.com/BernardUriza/portfolio-spring/settings/security_analysis
```

**Frontend:**
```
https://github.com/BernardUriza/portfolio-frontend/settings/security_analysis
```

**Enable these features:**

1. **✅ Dependabot alerts**
   - Notifies you of vulnerable dependencies
   - Check ☑️ "Dependabot alerts"

2. **✅ Dependabot security updates**
   - Auto-creates PRs for security vulnerabilities
   - Check ☑️ "Dependabot security updates"

3. **✅ Dependabot version updates** (Already configured)
   - Weekly PRs for dependency updates
   - Configured in `dependabot.yml`

4. **✅ Code scanning alerts** (Optional)
   - Check ☑️ "Code scanning"
   - Select "CodeQL Analysis" for Java/TypeScript

---

### Step 2: Configure Notification Preferences

```
https://github.com/settings/notifications
```

**Dependabot settings:**
- ☑️ "Dependabot alerts"
- ☑️ "Security vulnerabilities"
- Choose: Email and/or Web

---

### Step 3: Review Dependabot PRs

**Dependabot will create PRs weekly (Mondays 09:00 UTC):**

1. **View PRs**:
   ```
   https://github.com/BernardUriza/portfolio-spring/pulls
   ```

2. **PR Format**:
   ```
   chore(deps): Bump spring-boot-starter from 3.5.0 to 3.5.1
   ```

3. **Review Checklist**:
   - ✅ Read changelog/release notes
   - ✅ Check CI/CD passes
   - ✅ Review breaking changes
   - ✅ Merge if safe

4. **Merge**:
   - Click **"Merge pull request"**
   - Or **"Squash and merge"**

---

## 6. ✅ Verification Checklist

### GitHub Secrets ✓

**Backend (portfolio-spring):**
- [ ] `RENDER_STAGING_DEPLOY_HOOK` added
- [ ] `RENDER_PROD_DEPLOY_HOOK` added
- [ ] `SLACK_WEBHOOK_URL` added (optional)

**Frontend (portfolio-frontend):**
- [ ] `STAGING_DEPLOY_TOKEN` added
- [ ] `PROD_DEPLOY_TOKEN` added
- [ ] `SLACK_WEBHOOK_URL` added (optional)

---

### Production Environment ✓

**Backend:**
- [ ] Environment `production` created
- [ ] Required reviewer: @BernardUriza
- [ ] Deployment branches: `main` or `prod`
- [ ] Protection rules saved

**Frontend:**
- [ ] Environment `production` created
- [ ] Required reviewer: @BernardUriza
- [ ] Deployment branches: `main` or `prod`

---

### Pipeline Testing ✓

**Staging:**
- [ ] Pushed to `develop` branch
- [ ] Workflow triggered and ran
- [ ] All jobs completed successfully
- [ ] Staging deployed to Render/Netlify
- [ ] Slack notification received (if configured)

**Production:**
- [ ] Pushed to `main` branch
- [ ] Workflow paused for approval
- [ ] Email notification received
- [ ] Manually approved deployment
- [ ] Production deployed successfully
- [ ] Slack success notification received

---

### Dependabot ✓

- [ ] Dependabot alerts enabled
- [ ] Security updates enabled
- [ ] Version updates configured (weekly)
- [ ] Notification preferences set
- [ ] First PR received and reviewed

---

### Slack Notifications ✓

- [ ] Slack app created
- [ ] Incoming webhook enabled
- [ ] Webhook URL added to GitHub secrets
- [ ] Test notification sent successfully
- [ ] Staging deployment notification received
- [ ] Production deployment notification received

---

## 🎯 Success Criteria

You've successfully configured the CI/CD pipeline when:

✅ **Secrets are configured** - All required secrets added to both repos
✅ **Environments are protected** - Production requires manual approval
✅ **Staging auto-deploys** - Push to `develop` triggers automatic deployment
✅ **Production has approval** - Push to `main` requires review before deploy
✅ **Notifications work** - Slack/email notifications received
✅ **Dependabot is active** - Weekly PRs for dependency updates

---

## 🆘 Troubleshooting

### Secret not working

**Problem**: Workflow can't access secret

**Solution**:
1. Verify secret name matches exactly (case-sensitive)
2. Secret value has no trailing spaces
3. Re-create secret if needed
4. Check workflow syntax: `${{ secrets.SECRET_NAME }}`

---

### Deployment fails

**Problem**: Render/Netlify deployment fails

**Solution**:
1. Verify deploy hook URL is correct
2. Check Render/Netlify dashboard for errors
3. Ensure service is active and not paused
4. Test deploy hook manually with curl

---

### No approval prompt

**Problem**: Production deploys without approval

**Solution**:
1. Verify environment name matches exactly: `production`
2. Check workflow uses: `environment: production`
3. Ensure protection rules are saved
4. Branch must match deployment branch rule

---

### Slack notifications not received

**Problem**: No Slack messages

**Solution**:
1. Verify webhook URL is correct
2. Test webhook with curl (see Step 4)
3. Check Slack app is installed to workspace
4. Ensure webhook has permission to post
5. Verify secret `SLACK_WEBHOOK_URL` exists

---

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub Environments](https://docs.github.com/en/actions/deployment/targeting-different-environments)
- [Dependabot Configuration](https://docs.github.com/en/code-security/dependabot)
- [Render Deploy Hooks](https://render.com/docs/deploy-hooks)
- [Slack Incoming Webhooks](https://api.slack.com/messaging/webhooks)
- [Netlify Deploy Notifications](https://docs.netlify.com/configure-builds/build-hooks/)

---

**Last Updated**: 2025-10-25
**Status**: Ready for Configuration
**Next**: Start with [Section 1: Configure Secrets](#1-configure-github-secrets)
