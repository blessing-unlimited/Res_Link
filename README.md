# ResLink

**Find. Connect. Live.**

A student-residence app by **Dynamic Developers** (INSY72315).

**Open the app:** [https://reslink-zkca.onrender.com](https://reslink-zkca.onrender.com)

**Project infographic:** [https://reslink-infographic-zkca.onrender.com](https://reslink-infographic-zkca.onrender.com)

**Group presentation video:** [ResLink-Presentation.mp4 on the GitHub release](https://github.com/blessing-unlimited/Res_Link/releases/tag/presentation-video). It is about 40 minutes long: Thami starts at 0:03, Sihle at 5:28 and Blessing at 17:01. Download the file from the release page; it plays in any browser or media player.

The first visit can take about a minute while the free host wakes up. Then sign in with one of the demo accounts:

| Role | Email | Password |
| --- | --- | --- |
| Student | student@reslink.app | Student123! |
| Residence Admin | admin@reslink.app | Admin123! |
| Security | security@reslink.app | Security123! |
| Maintenance | maintenance@reslink.app | Maintenance123! |

Staff self-registration uses the access code `RESLINK-STAFF-2026`.

The same API and accounts work in a local browser or in Android Studio. The Android app talks to the live backend at `https://reslink-zkca.onrender.com/` by default, so it works on an emulator or a real phone without running the API locally.

## What the lecturer can mark

1. Watch the [group presentation video](https://github.com/blessing-unlimited/Res_Link/releases/tag/presentation-video).
2. Open [https://reslink-zkca.onrender.com](https://reslink-zkca.onrender.com) or run the app locally (steps below).
3. Sign in with each demo account and follow the walkthrough.
4. Run `dotnet test ResLink.sln` from the repository root.
5. API documentation is at [https://reslink-zkca.onrender.com/swagger](https://reslink-zkca.onrender.com/swagger) or `http://localhost:5180/swagger`.

ResLink is one app for students, residence administrators, security, and maintenance. Each role has its own sign-up path, home screen, and permissions. The browser uses the house-and-chain logo, Plus Jakarta Sans type, and a dark green (#04180f) sidebar that matches the logo background. Each feature has its own colour, and the account role sits under the person's name. Event cards use stock photos. On a tablet the sidebar shrinks to icons, and on a phone the menu moves to a bottom bar. The Android app uses the same colours, with a dark side rail on tablets.

## Purpose

The app shows how a residence can:

- improve student communication through a community feed, events, study groups, and a marketplace
- speed up support through maintenance tickets and in-app notifications
- strengthen safety through a panic button and a noise-complaint workflow
- reward participation with points that can be redeemed in the app
- give staff role-specific dashboards instead of a single shared login

## Team contributions

ResLink is built by **Dynamic Developers**.

| Member | GitHub | Contribution |
| --- | --- | --- |
| Sihle | [@Sihl3s](https://github.com/Sihl3s) | The first working prototype, hosting and data (Docker, Render, Neon), the web and Android redesigns, password reset, bug fixes from group testing, the automated tests, hosting the infographic, and this run guide. See Sihle's work below. |
| Thami (Nomathamsanqa) | [@NomathamsanqaTame](https://github.com/NomathamsanqaTame) | Designed the ResLink logo, built the infographic website that presents the project, and recorded her part of the group video. See Thami's work below. |
| Blessing | [@blessing-unlimited](https://github.com/blessing-unlimited) | Created the group repository and leads the backend. Built maintenance photo upload with Cloudinary, the PostgreSQL support the live app runs on, push notifications with Firebase, password reset requests, live location for panic alerts, and reward vouchers, and tested the app on Android. See Blessing's work below. |

## Prerequisites

- .NET 9 SDK (required to run the app in a browser on your own machine)
- Android Studio and an emulator, only if you also want to run the mobile app
- Android SDK path set in `android/local.properties` (copy from `android/local.properties.example`)

## How to run

### 1. View the app in a browser

From the repository root:

```bash
cd backend/ResLink.Api
dotnet run --launch-profile http
```

Then open:

**http://localhost:5180**

That page is the ResLink app (sign-in, role dashboards, and all features).  
API documentation remains available at [http://localhost:5180/swagger](http://localhost:5180/swagger).

Leave this terminal open while you use the app.

Local runs create `backend/ResLink.Api/reslink.db` the first time and never change it afterwards. If you ran an older version and something like photo upload fails, stop the API, delete `reslink.db`, and start it again to get a fresh database with the demo accounts.

### 2. Run the Android application (optional)

1. Copy `android/local.properties.example` to `android/local.properties` and set the Android SDK path.
2. In Android Studio, open the `android` folder and wait for Gradle sync to finish.
3. Start an emulator or connect a phone, then run the **app** configuration.

The app calls the live API at `https://reslink-zkca.onrender.com/` (`API_BASE_URL` in `android/app/build.gradle.kts`). The first request can take about a minute while the free host wakes up.

To test against the API on your own computer instead, change `API_BASE_URL` to `http://10.0.2.2:5180/` for the emulator, or to the computer's LAN address (for example `http://192.168.1.10:5180/`) for a phone on the same Wi-Fi network.

## Demo accounts

The database is created and seeded automatically the first time the API starts.

| Role | Email | Password |
| --- | --- | --- |
| Student | student@reslink.app | Student123! |
| Residence Admin | admin@reslink.app | Admin123! |
| Security | security@reslink.app | Security123! |
| Maintenance | maintenance@reslink.app | Maintenance123! |

New **student** accounts can be created from the student sign-up screen (name, email, student number, residence, and room).

New **staff** accounts require the access code `RESLINK-STAFF-2026`. This stops a student from registering as an administrator, security officer, or maintenance technician.

## Role-based access

After login, the API returns a JWT that includes the user’s role. The browser and Android app then open the correct dashboard.

| Feature | Student | Admin | Security | Maintenance |
| --- | --- | --- | --- | --- |
| Community feed and polls | Yes | Yes (can moderate) | — | — |
| Events and RSVP | RSVP | Create and manage | — | — |
| Marketplace | Buy / sell | Oversight | — | — |
| Maintenance tickets | Create and track own | View all | — | Update status |
| Noise complaints | Submit (optional anonymous) | View | Inbox and resolve | — |
| Panic button | Trigger | View | Inbox and respond | — |
| Rewards and study groups | Yes | View / moderate | — | — |
| Analytics and user management | — | Yes | — | — |

## Suggested walkthrough for marking

1. **Student** (`student@reslink.app`): open Home, Community Feed, Events (RSVP), Marketplace, Maintenance (report an issue with a photo), Panic, Noise complaint, Rewards, and Study Groups. RSVP to an event and then cancel it to see the points go up and back down.
2. **Security** (`security@reslink.app`): open the security desk, acknowledge the seeded panic alert, and resolve the noise complaint.
3. **Maintenance** (`maintenance@reslink.app`): open the ticket board and move the leaking-tap ticket from Open to In Progress, then Resolved.
4. **Admin** (`admin@reslink.app`): review analytics, create an event, moderate a feed post, and deactivate or reactivate a user.
5. **Registration check:** create a new student account, then attempt staff sign-up without the access code to confirm the request is rejected.

## Automated tests

From the repository root:

```bash
dotnet test ResLink.sln
```

The tests confirm:

- a student can log in and is blocked from admin analytics
- staff registration fails without the correct access code
- security can read the emergency queue
- a new student can register and receive a JWT
- password reset works without email, and an admin reset link can only be used once
- cancelling an RSVP, leaving a study group, deleting a post, cancelling a maintenance ticket, or withdrawing a noise complaint takes back the points that action earned
- a maintenance photo uploads without Cloudinary, can be viewed, and files that are not images are rejected
- times on new items are sent in UTC, so a post made just now shows as "just now" in every time zone
- a full study group cannot be joined, and a reward with no stock left cannot be redeemed
- a panic alert sent without a location carries the student's room and residence name

## Points

Students earn points for taking part: 10 for an event RSVP, 5 for a post, 5 for joining or creating a study group, and 8 for a maintenance ticket or noise complaint. Undoing any of these takes the same points back, so RSVPing and cancelling over and over earns nothing. If a student spends points and then undoes the action that earned them, the balance can drop below zero until they earn the points again. Staff reopening a cancelled ticket or a withdrawn complaint gives the points back.

## Sihle's work

Sihle built the first working version of ResLink, keeps it hosted, and fixed and redesigned it after group testing.

### Prototype and hosting

- Published the first working prototype in August 2026: the ASP.NET Core API, the browser app, the Android app, the demo accounts and data, and the Render hosting files. Added the Harvard in-text comments and the reference list.
- Brought the app onto this group repository and stopped browsers from keeping an old copy of the app after an update.
- Wrote the `Dockerfile` and `render.yaml`, and set up the live Render service in Frankfurt.
- Connected the live Render service to the Neon database through Blessing's PostgreSQL support, so live data survives a restart, and checked that a post survives a redeploy. An earlier version of the PostgreSQL work ([#2](https://github.com/blessing-unlimited/Res_Link/pull/2)) was closed because Blessing's version reached `main` first; its README steps and a UTC time fix were carried over.
- Moved hosting to this repository when the old one was retired, and pointed the Android app at the live API so it works on any phone without running the API locally.

### Fixes

- Sign-up: the residence list always loads and shows an error if it cannot, and the API refuses to start in production without a JWT key ([#8](https://github.com/blessing-unlimited/Res_Link/pull/8)).
- Password reset: built on Blessing's reset tokens and email service with a "Forgot password?" page on the web and Android, a page to set the new password, an admin "Create reset link" button for when email is not set up, and reset links that use the live `https://` address.
- Panic button: brought Blessing's live location to the web, so the web app also sends the student's location and the web security desk shows a map link.
- Rewards: removed the reward undo button, so a redeemed voucher can only be voided by an admin.
- Admins can mark marketplace items as sold on the web, as the API already allowed.

### Redesign

- Cropped Thami's logo into sharp icon, full logo and horizontal files for the web, Android and the infographic.
- Redesigned the web app around the logo colours ([#9](https://github.com/blessing-unlimited/Res_Link/pull/9)): a dark green sidebar, sign-in and sign-up tabs, pop-up messages, and layouts for phones, tablets and laptops.
- Redesigned the Android app to match: the same theme and components, a brand header on sign-in, and a dark side rail on tablets.

### Infographic

- Redesigned Thami's infographic page, enlarged the header logo, and matched the header to the logo colours ([#10](https://github.com/blessing-unlimited/Res_Link/pull/10)).
- Hosted it on Render as a static site ([#11](https://github.com/blessing-unlimited/Res_Link/pull/11)) and added the Dynamic Developers name and GitHub usernames to the team section ([#12](https://github.com/blessing-unlimited/Res_Link/pull/12)).

### Fixes from Blessing's testing

- Stopped points farming: undoing an action now takes back the points it earned (see Points above).
- Maintenance photos: replaced the "Photo URL" text box with a camera and gallery upload on Android and a photo picker on the web, and show the photo on each ticket. Photos are stored in the database until Cloudinary is set up.
- Android layout: the "Community" label in the bottom bar no longer wraps, and long titles or locations no longer squeeze status labels until their text stacks vertically. Staff status buttons wrap onto a new line on narrow phones.
- Web layout: on phones the maintenance filter row no longer makes the whole page wider than the screen.

### Later fixes

- Android: the points on the Rewards and Profile screens refresh from the server, so points earned or taken back elsewhere show straight away instead of after signing in again.
- Local runs: times read back from SQLite are marked as UTC, so something posted just now no longer shows as "2 h ago".
- Study groups: a full group can no longer be joined, and the web and Android apps show "Group is full" instead of a Join button.
- Rewards: a reward with no stock left can no longer be redeemed, and the apps show "Out of stock".
- Panic button: an alert sent without the phone's location now names the student's residence instead of just "Residence".
- Android: when the server turns a request down, the app shows the reason (for example "This study group is full.") instead of "Request failed (400)".

### Tests and documentation

- Wrote the automated tests, now 18 of them (see Automated tests above).
- Wrote and maintain this run guide, the hosting steps, and the demo walkthrough.

## Thami's work

Thami designed the ResLink logo and built the project's infographic website.

### Logo

- Designed the house-and-chain ResLink logo (`reslink.jpeg`, August 2026). The icon, full logo and horizontal logo files used by the web app, the Android app and the infographic (`docs/brand/`) are all cropped from it.

### Infographic website

- Built the infographic page, `docs/index.html` (September 2026). It explains ResLink to someone who has not opened the app, without being a second copy of the app.
- The page shows the logo and the line "Find. Connect. Live.", who the app is for (students, admin, security and maintenance), four short blocks on community, events, safety and maintenance, a link to the live app, and the team.

**Live site:** [https://reslink-infographic-zkca.onrender.com](https://reslink-infographic-zkca.onrender.com)

In the Meet the Team section, hovering over (or tabbing to) a name shows that member's GitHub username, and clicking it opens their GitHub profile. Render hosts the page as a free static site defined in `render.yaml`, and it updates whenever a change to `docs/` reaches `main`.

### Group video

- Recorded her part of the group video, explaining the work she contributed. Each member recorded their own part, and the parts were joined into one [presentation video](https://github.com/blessing-unlimited/Res_Link/releases/tag/presentation-video) with a ResLink intro and outro.

## Blessing's work

Blessing created the group repository and leads the backend. After the prototype was in place, he added these features, each through its own pull request.

### Maintenance photo upload and secrets ([#1](https://github.com/blessing-unlimited/Res_Link/pull/1))

- Added `POST /api/maintenance/photos`, which checks that a photo is a JPEG or PNG under 5 MB and uploads it to a Cloudinary maintenance folder with the Cloudinary .NET SDK (Cloudinary, 2026).
- Moved the API keys and other settings out of `appsettings.json` into .NET user secrets so they are not stored in Git.
- Added PostgreSQL support: when `DATABASE_URL` is set, the API uses PostgreSQL, and otherwise SQLite. In [#4](https://github.com/blessing-unlimited/Res_Link/pull/4) he regenerated the PostgreSQL migration, read Neon's connection string into a proper Npgsql connection with SSL required, and applied migrations on start-up. This is the code the live app runs on today.

### Push notifications ([#3](https://github.com/blessing-unlimited/Res_Link/pull/3), [#4](https://github.com/blessing-unlimited/Res_Link/pull/4))

- Added Firebase Cloud Messaging to the Android app. It asks for notification permission on Android 13 and later, and sends the phone's push token to `PUT /api/auth/push-token`.
- Added `FirebasePushService` on the server, which sends a push alongside every in-app notification. This covers panic alerts and noise complaints for security, new tickets for maintenance, ticket updates for the student, and new RSVPs for admins (Firebase, 2026a; Firebase, 2026b; Firebase, 2026c).
- Kept `google-services.json` and the Firebase server key out of Git. Pushes start once the Firebase keys are set (see Hosting and configuration).

### Password reset requests ([#5](https://github.com/blessing-unlimited/Res_Link/pull/5))

- Added `POST /api/auth/forgot-password` with one-time reset tokens that are stored as hashes and expire after 30 minutes.
- Added the email service that sends the reset link through SendGrid when an email key is set.

### Live location for panic alerts ([#6](https://github.com/blessing-unlimited/Res_Link/pull/6))

- The Android app asks for location permission when the student presses the panic button and sends the phone's latitude and longitude with the alert (Android Developers, 2025d).
- The security screen on Android shows the coordinates and an **Open in Maps** button. If the student refuses permission, the alert still carries their room and residence.

### Reward vouchers ([#7](https://github.com/blessing-unlimited/Res_Link/pull/7))

- Redeeming a reward now creates a one-time voucher code that the student is shown and can find again under their recent redemptions.
- Added a **Redemptions** tab for admins to mark a code as used or void it. Voiding a code returns the student's points and the reward's stock.

### Testing

- Tested the app on Android and recorded the problems he found: points could be farmed by RSVPing and cancelling, the "Community" label in the bottom bar was cut off, the maintenance form asked for a photo URL instead of a file or the camera, and long ticket titles squeezed the status text until it stacked vertically. These were fixed in [#13](https://github.com/blessing-unlimited/Res_Link/pull/13).

## Hosting and configuration

Do not put passwords, API keys, or connection strings in Git. Set them in the Render dashboard under **Environment** (Render, 2025).

### Render

The live app is a Render web service built from this repository (`blessing-unlimited/Res_Link`). `render.yaml` describes it: Docker, the free plan, and the Frankfurt region, the same as the Neon database (Render, 2025).

1. On Render, choose **New > Blueprint** and connect `blessing-unlimited/Res_Link`. A repository owner may have to approve the Render GitHub app first.
2. Render reads `render.yaml`, creates the `reslink` web service and the `reslink-infographic` static site, and generates `Jwt__Key` itself. Render may add a short suffix to each name, which is why the live addresses end in `-zkca`.
3. When it asks for the other values, set `Staff__AccessCode` and `DATABASE_URL`. The Cloudinary, Firebase and email values can stay empty until those accounts exist.
4. Each push to `main` redeploys the service. Password reset links use the service's own address automatically.

### Neon database

Render's free disk is wiped when the service sleeps, so the live data is kept in a Neon PostgreSQL database, because Neon's free plan does not expire the way Render's free PostgreSQL does (Neon, 2026; Render, 2026). When `DATABASE_URL` is set the API uses PostgreSQL and applies the migrations in `backend/ResLink.Api/Migrations`; otherwise it uses SQLite, so local runs and tests need no database server (Microsoft, 2025c).

1. In the Neon project (region: Frankfurt), open **Connect**, turn **Connection pooling** off, and copy the connection string. It starts with `postgresql://`.
2. On the Render web service, set `DATABASE_URL` to that string. Do not paste it into Git or the group chat.
3. On first start the API creates the tables and loads the demo accounts, but only into an empty database.
4. After changing an entity class, add a migration from `backend/ResLink.Api` with a placeholder URL so it is generated for PostgreSQL: `DATABASE_URL="postgresql://design:design@localhost/reslink" dotnet ef migrations add DescribeTheChange`.

### Settings

`appsettings.json` keeps the JWT signing key and the staff access code empty. Local runs generate a temporary key and use the demo staff code from `appsettings.Development.json`. The hosted app refuses to start without a JWT key, so the values live only on the server (OWASP, 2023).

| Setting on Render | What it does | If it is empty |
| --- | --- | --- |
| `Jwt__Key` | Signs sign-in tokens. Render generates it; choose **Generate** to rotate it, after which everyone signs in again. | The hosted app does not start. |
| `Staff__AccessCode` | The code staff need to register. | Staff cannot register. |
| `DATABASE_URL` | The Neon connection string. | Data is kept in SQLite and lost when the service sleeps. |
| `Cloudinary__CloudName`, `Cloudinary__ApiKey`, `Cloudinary__ApiSecret` | Store maintenance photos in Cloudinary. | Photos are kept in the database, so they still survive a restart. |
| `Firebase__ServiceAccountJson` | The Firebase server key for push notifications. | In-app notifications still work, but no pushes are sent. |
| `Email__ApiKey`, `Email__From` | SendGrid key and sender for reset emails. | No email is sent; an admin uses **Admin Panel → Residents → Create reset link** and sends the link to the resident. |

## Next steps for a real residence

The app works end to end for the demo. These steps need accounts, contracts or a residence to work with, so they are left for when a residence takes ResLink on.

1. **Turn on push notifications.** Add `google-services.json` from the Firebase project (package `com.dynamicdevelopers.reslink`) to `android/app` without committing it, set `Firebase__ServiceAccountJson` on Render, and test with the security phone locked (Google, 2025).
2. **Move photos to Cloudinary.** Set the three Cloudinary values on Render. New photos then go to Cloudinary; older ones stay in the database and still load.
3. **Send reset emails.** Verify the residence's domain with SendGrid, then set `Email__ApiKey` and `Email__From` (such as `noreply@<residence domain>`).
4. **Change the staff code.** Replace `RESLINK-STAFF-2026` on Render with a new code and share it only with residence staff.
5. **Reach security outside the app.** Send one SMS with the student's name, room and residence to the duty officer when a panic alert is raised, through Twilio or a similar service.
6. **Connect vouchers to a partner.** Vouchers already have one-time codes; a cafe till or laundry vendor needs to accept them. Never store card numbers in ResLink (OWASP, 2023).
7. **QR check-in at events.** Generate a random code per event, show it as a QR code, and award attendance points once per student who scans it.
8. **Backups and a custom domain.** Set up daily Neon snapshots kept for at least seven days if the plan allows it (Neon, 2026), add the residence's domain in Render, and write the restore steps in this file.

## Project structure

```text
ResLink/
  backend/ResLink.Api/          ASP.NET Core Web API and browser app
  backend/ResLink.Api.Tests/    Authentication and role tests
  android/                      Kotlin Jetpack Compose application
  docs/                         Infographic website (index.html and brand/ logos)
  render.yaml                   Render services for the app and the infographic
  Dockerfile                    Container build used by Render
  reslink.jpeg                  App logo
  README.md                     This guide
```

## References

Android Developers (2025a) *Jetpack Compose*. Available at: https://developer.android.com/compose (Accessed: 25 September 2026).

Android Developers (2025b) *Material Design 3 in Compose*. Available at: https://developer.android.com/develop/ui/compose/designsystems/material3 (Accessed: 25 September 2026).

Android Developers (2025c) *Build adaptive navigation*. Available at: https://developer.android.com/develop/ui/compose/layouts/adaptive/build-adaptive-navigation (Accessed: 2 October 2026).

Android Developers (2025d) *Get the last known location*. Available at: https://developer.android.com/develop/sensors-and-location/location/retrieve-current (Accessed: 5 October 2026).

Android Developers (2025e) *Photo picker*. Available at: https://developer.android.com/training/data-storage/shared/photo-picker (Accessed: 5 October 2026).

Cloudinary (2026) *.NET SDK*. Available at: https://cloudinary.com/documentation/dotnet_integration (Accessed: 5 October 2026).

Firebase (2026a) *Add the Firebase Admin SDK to your server*. Available at: https://firebase.google.com/docs/admin/setup (Accessed: 5 October 2026).

Firebase (2026b) *Send messages to multiple devices*. Available at: https://firebase.google.com/docs/cloud-messaging/send-message (Accessed: 5 October 2026).

Firebase (2026c) *Set up a Firebase Cloud Messaging client app on Android*. Available at: https://firebase.google.com/docs/cloud-messaging/android/client (Accessed: 5 October 2026).

GitHub (2026) *Octicons*. Available at: https://primer.style/octicons/ (Accessed: 5 October 2026).

Google (2025) *Firebase Cloud Messaging*. Available at: https://firebase.google.com/docs/cloud-messaging (Accessed: 25 September 2026).

Google Fonts (2026) *Plus Jakarta Sans*. Available at: https://fonts.google.com/specimen/Plus+Jakarta+Sans (Accessed: 2 October 2026).

Lucide (2026) *Lucide icons*. Available at: https://lucide.dev/ (Accessed: 5 October 2026).

Material Design (2025) *Icons*. Available at: https://m3.material.io/styles/icons/overview (Accessed: 25 September 2026).

MDN (2025) *Using the Fetch API*. Available at: https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch (Accessed: 25 September 2026).

Microsoft (2024) *PasswordHasher of ASP.NET Core Identity*. Available at: https://learn.microsoft.com/dotnet/api/microsoft.aspnetcore.identity.passwordhasher-1 (Accessed: 25 September 2026).

Microsoft (2025a) *Overview of ASP.NET Core*. Available at: https://learn.microsoft.com/aspnet/core/introduction-to-aspnet-core?view=aspnetcore-9.0 (Accessed: 25 September 2026).

Microsoft (2025b) *Authentication and authorization in ASP.NET Core*. Available at: https://learn.microsoft.com/aspnet/core/fundamentals/minimal-apis/security?view=aspnetcore-9.0 (Accessed: 25 September 2026).

Microsoft (2025c) *SQLite EF Core database provider*. Available at: https://learn.microsoft.com/ef/core/providers/sqlite/ (Accessed: 25 September 2026).

Microsoft (2025d) *Get started with Swashbuckle and ASP.NET Core*. Available at: https://learn.microsoft.com/aspnet/core/tutorials/getting-started-with-swashbuckle (Accessed: 25 September 2026).

Microsoft (2025e) *Role-based authorization in ASP.NET Core*. Available at: https://learn.microsoft.com/aspnet/core/security/authorization/roles?view=aspnetcore-9.0 (Accessed: 25 September 2026).

Neon (2026) *Neon documentation*. Available at: https://neon.com/docs (Accessed: 2 October 2026).

OpenAPI Initiative (2024) *OpenAPI Specification*. Available at: https://spec.openapis.org/oas/latest.html (Accessed: 25 September 2026).

OWASP (2023) *Authentication cheat sheet*. Available at: https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html (Accessed: 25 September 2026).

Render (2025) *Docker on Render*. Available at: https://render.com/docs/docker (Accessed: 25 September 2026).

Render (2026) *Deploy for free*. Available at: https://render.com/docs/free (Accessed: 2 October 2026).

SQLite Consortium (2025) *About SQLite*. Available at: https://www.sqlite.org/about.html (Accessed: 25 September 2026).

Square (2024) *Retrofit*. Available at: https://square.github.io/retrofit/ (Accessed: 25 September 2026).

Unsplash (2026) *License*. Available at: https://unsplash.com/license (Accessed: 25 September 2026).

Firebase (2026a) Firebase admin .NET SDK release notes, Firebase.com. Available at: https://firebase.google.com/support/release-notes/admin/dotnet (Accessed: October 3, 2026).

Firebase (2026b) FirebaseAdmin.Messaging.FirebaseMessaging, Firebase.com. Available at: https://firebase.google.com/docs/reference/admin/dotnet/class/firebase-admin/messaging/firebase-messaging (Accessed: October 3, 2026).

Firebase (2026c) Send a message using FCM HTTP v1 API, Firebase.com. Available at: https://firebase.google.com/docs/cloud-messaging/send/v1-api (Accessed: October 3, 2026).

Firebase (2026d) Send a message using Firebase Admin SDK, Firebase.com. Available at: https://firebase.google.com/docs/cloud-messaging/send/admin-sdk (Accessed: October 3, 2026).

Maze, C. (2024) Different ways to validate an uploaded file in Asp.net core, Code Maze. Available at: https://code-maze.com/aspnetcore-validate-uploaded-file/ (Accessed: October 5, 2026).


