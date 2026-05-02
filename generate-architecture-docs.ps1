$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

function Escape-Html {
    param([string]$Text)

    if ($null -eq $Text) {
        return ""
    }

    return $Text.Replace('&', '&amp;').Replace('<', '&lt;').Replace('>', '&gt;').Replace('"', '&quot;')
}

function Get-FileDescription {
    param(
        [string]$ModuleName,
        [string]$RelativePath
    )

    switch -Regex ($RelativePath) {
        '^pom\.xml$' { return "Build descriptor defining dependencies, Java version, plugins, and the Spring Boot packaging for $ModuleName." }
        '^package\.json$' { return "Frontend dependency and script manifest used to install, build, serve, and test the Angular application." }
        '^angular\.json$' { return "Angular workspace configuration describing build targets, dev-server behavior, proxying, and optimization settings." }
        '^README\.md$' { return "Human-readable runbook describing what the module does, how to start it, and what endpoints or features it exposes." }
        'Application\.java$' { return "Spring Boot entry point that starts the module and wires the top-level framework configuration." }
        '\\config\\' { return "Configuration class that connects framework behavior, routing, security, OpenAPI, scheduling, or websocket support to this module." }
        '\\controller\\' { return "HTTP or websocket controller that exposes the module's API contract to callers through REST endpoints or STOMP mappings." }
        '\\service\\' { return "Business-logic class or contract responsible for orchestrating workflows, validation, integrations, and core rules." }
        '\\repository\\' { return "Data-access layer used to read and write persisted records through Spring Data JPA." }
        '\\entity\\' { return "Persisted domain model representing the data owned by this module." }
        '\\dto\\' { return "Request/response contract object that keeps transport payloads cleanly separated from internal entities." }
        '\\security\\' { return "Security support code such as JWT parsing, user-details adaptation, or request authentication filters." }
        '\\oauth\\' { return "OAuth helper code that maps provider identities into ConnectSphere users and token responses." }
        'application.*\.yml$' { return "Runtime configuration profile controlling ports, datasource behavior, service-to-service URLs, and environment-specific settings." }
        'src\\test\\' { return "Automated test covering this module's runtime behavior and helping verify that important flows remain working." }
        'src\\app\\core\\' { return "Shared Angular client logic used across multiple pages for session state, API requests, models, and realtime communication." }
        'src\\app\\pages\\' { return "Standalone Angular route/page implementing one user-facing slice of the ConnectSphere frontend." }
        'src\\app\\app\.(ts|html|scss)$' { return "Root Angular shell for global layout, session controls, and router hosting." }
        'src\\app\\app\.routes\.ts$' { return "Angular route table mapping URL paths to the major frontend pages." }
        'src\\proxy\.conf\.json$' { return "Angular dev-server proxy so frontend requests can reach the gateway and websocket endpoints during local development." }
        default { return "Supporting source file that contributes to the module's implementation, build, or verification story." }
    }
}

function Get-FileRows {
    param(
        [string]$ModulePath,
        [string]$ModuleName
    )

    $files = Get-ChildItem -Path $ModulePath -Recurse -File |
        Where-Object {
            $_.FullName -notmatch '\\target\\' -and
            $_.FullName -notmatch '\\node_modules\\' -and
            $_.FullName -notmatch '\\dist\\'
        } |
        Where-Object {
            $_.Name -in @('pom.xml', 'README.md', 'package.json', 'angular.json') -or
            $_.FullName -like "*\src\*"
        } |
        Sort-Object FullName

    $rows = New-Object System.Collections.Generic.List[string]
    foreach ($file in $files) {
        $relativePath = $file.FullName.Substring($ModulePath.Length + 1)
        $rows.Add("<tr><td><code>$(Escape-Html $relativePath)</code></td><td>$(Escape-Html (Get-FileDescription -ModuleName $ModuleName -RelativePath $relativePath))</td></tr>")
    }

    return @{
        Rows = $rows
        Count = $files.Count
    }
}

$modules = @(
    [pscustomobject]@{
        Name = 'auth-service'
        Title = 'Auth Service Architecture Guide'
        Type = 'Spring Boot microservice'
        Port = '8081'
        Database = 'connectsphere_auth (MySQL) / authdb (H2 tests)'
        Protocols = 'REST, JWT, OAuth2, Spring Security'
        Summary = @(
            'The auth-service is the identity and security foundation of ConnectSphere. It creates accounts, authenticates users, issues JWT access and refresh tokens, supports profile editing, and exposes user-search and token-validation endpoints for the rest of the platform.',
            'Internally it combines Spring Security, JPA entities, repositories, service-layer rules, JWT creation and rotation, and optional Google/GitHub OAuth login.'
        )
        Responsibilities = @('User registration and login', 'JWT access-token and refresh-token lifecycle', 'Profile and password management', 'OAuth account onboarding', 'User lookup and downstream token validation')
        Flows = @(
            'Register flow: controller validates the request, service creates the user, password is bcrypt-hashed, token service issues access and refresh tokens, and the API returns the full AuthResponse.',
            'Login flow: AuthenticationManager verifies credentials, JwtTokenService creates a fresh token pair, and the response contains expiry timestamps plus the user profile.',
            'Refresh flow: refresh token is validated, old refresh token is revoked, and the service rotates a new token pair so long-lived sessions stay safer.',
            'Protected request flow: JwtAuthenticationFilter extracts the bearer token, validates it, builds the Spring Security Principal, and then protected controllers can rely on Principal for the current user.',
            'OAuth flow: the OAuth classes convert Google/GitHub identity data into a ConnectSphere user, then return the same JWT-based login model used by local authentication.'
        )
        Integrations = @('Used by search-service for user search', 'Supports frontend login/profile screens', 'Designed to sit behind the gateway for versioned API access')
        Endpoints = @('/api/v1/auth/register', '/api/v1/auth/login', '/api/v1/auth/refresh', '/api/v1/auth/profile', '/api/v1/auth/search', '/oauth2/authorization/google', '/oauth2/authorization/github')
        Verify = 'mvn test'
    },
    [pscustomobject]@{
        Name = 'post-service'
        Title = 'Post Service Architecture Guide'
        Type = 'Spring Boot microservice'
        Port = '8082'
        Database = 'connectsphere_post (MySQL) / postdb (H2 tests)'
        Protocols = 'REST, JPA'
        Summary = @(
            'The post-service owns the core social content layer: creating, updating, searching, soft-deleting, and serving posts for profile timelines and feeds.',
            'It also keeps reaction and comment counters, handles visibility changes, and best-effort syncs hashtags to search-service for indexing.'
        )
        Responsibilities = @('Post CRUD and soft deletion', 'Visibility control', 'Feed generation from followed user ids', 'Counter updates for likes/comments', 'Hashtag indexing trigger to search-service')
        Flows = @(
            'Create post flow: REST controller validates the post payload, service persists a Post entity, then a best-effort call sends content to search-service for hashtag extraction.',
            'Feed flow: the controller accepts followed user ids, repository queries posts for those authors, and the service returns newest-first feed items.',
            'Update and delete flow: service mutates the existing Post record, preserves soft-deletion semantics, and reindexes or removes hashtags when content changes.',
            'Counter flow: like-service and comment-service can call the increment/decrement endpoints so the post card can show denormalized counts without expensive aggregation.'
        )
        Integrations = @('Consumes followed user ids from feed callers', 'Syncs hashtags to search-service', 'Used by frontend feed, explore, and profile pages')
        Endpoints = @('/api/v1/posts', '/api/v1/posts/feed', '/api/v1/posts/user/{authorId}', '/api/v1/posts/search', '/api/v1/posts/{postId}/likes/increment', '/api/v1/posts/{postId}/comments/increment')
        Verify = 'mvn test'
    },
    [pscustomobject]@{
        Name = 'comment-service'
        Title = 'Comment Service Architecture Guide'
        Type = 'Spring Boot microservice'
        Port = '8083'
        Database = 'Dedicated comment schema / commentdb in tests'
        Protocols = 'REST, JPA'
        Summary = @(
            'The comment-service manages threaded discussion under posts. It stores top-level comments and replies, enforces thread depth rules, and supports editing, soft deletion, and comment-like counters.',
            'Its API is purposely narrow so the frontend or other services can attach comments to posts without duplicating discussion logic.'
        )
        Responsibilities = @('Create comments and replies', 'Enforce parent/child reply rules', 'Read comments by post or author', 'Soft-delete comments', 'Maintain like counters on comments')
        Flows = @(
            'Comment create flow: the controller accepts a CreateCommentRequest, service checks the parent rules, stores the Comment entity, and returns a CommentResponse.',
            'Reply flow: a reply can target a comment, but nested reply chains are intentionally constrained so the discussion stays manageable.',
            'Delete flow: the record is soft-deleted rather than physically removed, which supports moderation and audit-friendly behavior.',
            'Comment count flow: the service can count comments by post so post-service or frontend screens can show discussion volume.'
        )
        Integrations = @('Frontend feed page uses comment retrieval and creation', 'Post cards call comment count and reply endpoints', 'Can cooperate with like-service for comment reactions')
        Endpoints = @('/api/v1/comments', '/api/v1/comments/post/{postId}', '/api/v1/comments/{commentId}/replies', '/api/v1/comments/{commentId}/likes', '/api/v1/comments/count?postId=...')
        Verify = 'mvn test'
    },
    [pscustomobject]@{
        Name = 'like-service'
        Title = 'Like Service Architecture Guide'
        Type = 'Spring Boot microservice'
        Port = '8084'
        Database = 'Dedicated like schema / likedb in tests'
        Protocols = 'REST, JPA'
        Summary = @(
            'The like-service implements ConnectSphere''s polymorphic reaction system. A single service can attach a reaction to either a post or a comment while preserving one-reaction-per-user-per-target rules.',
            'It also exposes summary endpoints so callers can show total reactions or grouped reaction breakdowns without recalculating from raw rows every time.'
        )
        Responsibilities = @('Create or remove reactions', 'Ensure uniqueness of user+target+type', 'Return counts and grouped summaries', 'Support target types for posts and comments')
        Flows = @(
            'Reaction flow: controller receives target id, target type, user id, and reaction type, then the service enforces the uniqueness constraint before persisting.',
            'Unlike flow: the delete endpoint removes the existing relation so frontend or post/comment services can decrement denormalized counters.',
            'Summary flow: clients can ask for a grouped view of reactions to render badges or breakdowns on a post or comment.'
        )
        Integrations = @('Frontend feed page calls it when toggling likes', 'Can be paired with post/comment counter endpoints', 'Notification workflows can react to like creation')
        Endpoints = @('/api/v1/likes', '/api/v1/likes/has-liked', '/api/v1/likes/summary/{targetId}', '/api/v1/likes/count/{targetId}/type')
        Verify = 'mvn test'
    },
    [pscustomobject]@{
        Name = 'follow-service'
        Title = 'Follow Service Architecture Guide'
        Type = 'Spring Boot microservice'
        Port = '8085'
        Database = 'Dedicated follow schema / followdb in tests'
        Protocols = 'REST, JPA'
        Summary = @(
            'The follow-service owns the directed social graph between users. It stores who follows whom, returns follower/following lists and counts, and computes simple suggestions.',
            'When a new follow is created it also makes a best-effort notification call so users can be alerted without blocking the social-graph write.'
        )
        Responsibilities = @('Create and remove follow relationships', 'Return follower/following lists and counts', 'Check follow state quickly', 'Compute simple social suggestions', 'Send follow notifications best-effort')
        Flows = @(
            'Follow flow: the controller accepts follower and followee ids, the service prevents duplicates, persists the Follow entity, and tries to notify notification-service.',
            'Graph lookup flow: follower and following endpoints expose the relationship set so feed generation or profile screens can derive social context.',
            'Suggestion flow: the service looks at second-degree relationships to produce lightweight user suggestions.'
        )
        Integrations = @('Supplies followed user ids to feed generation callers', 'Pushes follow notifications to notification-service', 'Used heavily by profile and explore pages')
        Endpoints = @('/api/v1/follows', '/api/v1/follows/followers/{userId}', '/api/v1/follows/following/{userId}', '/api/v1/follows/suggested/{userId}', '/api/v1/follows/is-following')
        Verify = 'mvn test'
    },
    [pscustomobject]@{
        Name = 'notification-service'
        Title = 'Notification Service Architecture Guide'
        Type = 'Spring Boot microservice'
        Port = '8086'
        Database = 'Dedicated notification schema / notificationdb in tests'
        Protocols = 'REST, JPA'
        Summary = @(
            'The notification-service stores and exposes in-app activity for social events like follows, likes, or platform messages. It tracks read state, bulk creation, and unread counts.',
            'The current implementation also includes a stub-style email alert hook so future asynchronous channels can grow out of the same service boundary.'
        )
        Responsibilities = @('Persist in-app notifications', 'Mark single or all notifications as read', 'Return unread counts', 'Support bulk notification writes')
        Flows = @(
            'Create notification flow: a service caller posts a NotificationRequest, the module writes the Notification entity, and returns the API shape used by frontend pages.',
            'Inbox flow: the recipient endpoint returns notifications newest-first so the UI can render a per-user inbox view.',
            'Read-state flow: mark-one and mark-all endpoints mutate notification read flags while preserving the full history.'
        )
        Integrations = @('Receives best-effort follow notifications', 'Frontend notifications page reads inbox and unread counts', 'Can support future moderation or broadcast flows')
        Endpoints = @('/api/v1/notifications', '/api/v1/notifications/bulk', '/api/v1/notifications/recipient/{userId}', '/api/v1/notifications/read-all', '/api/v1/notifications/recipient/{userId}/unread-count')
        Verify = 'mvn test'
    },
    [pscustomobject]@{
        Name = 'media-service'
        Title = 'Media and Story Service Architecture Guide'
        Type = 'Spring Boot microservice'
        Port = '8087'
        Database = 'Dedicated media schema / mediadb in tests'
        Protocols = 'REST, multipart upload, scheduled cleanup'
        Summary = @(
            'The media-service handles uploaded files and ephemeral stories. It stores media metadata, serves local uploaded assets, and maintains 24-hour story records with a scheduled expiry job.',
            'The service acts like the platform''s lightweight media boundary so post and story features do not need to embed upload logic directly.'
        )
        Responsibilities = @('Upload and serve media files', 'Store media metadata linked to posts', 'Create and view stories', 'Expire old stories on a schedule', 'Soft-delete media and stories')
        Flows = @(
            'Upload flow: multipart request arrives with uploader id and file, the service stores the file under uploads/, persists metadata, and returns the public media URL.',
            'Story flow: a story upload records author, caption, media type, and expiry timestamp, then active story queries filter only currently valid records.',
            'Expiry flow: a scheduled cleanup job periodically marks expired stories inactive so the 24-hour behavior stays aligned with the case study.'
        )
        Integrations = @('Frontend stories page uploads and previews story media', 'Post-service can store returned media URLs on posts', 'Gateway routes file-serving and story endpoints')
        Endpoints = @('/api/v1/media/upload', '/api/v1/media/files/{filename}', '/api/v1/stories', '/api/v1/stories/active', '/api/v1/stories/{storyId}/view')
        Verify = 'mvn test'
    },
    [pscustomobject]@{
        Name = 'search-service'
        Title = 'Search and Hashtag Service Architecture Guide'
        Type = 'Spring Boot microservice'
        Port = '8088'
        Database = 'Dedicated search schema / searchdb in tests'
        Protocols = 'REST, JPA, delegated REST lookups'
        Summary = @(
            'The search-service extracts hashtags from post content, stores hashtag-to-post mappings, calculates trending data, and delegates full-text searches for users and posts to the owning services.',
            'It keeps hashtag logic centralized while still respecting microservice ownership boundaries for user and post records.'
        )
        Responsibilities = @('Extract hashtags from post content', 'Persist hashtag and post-tag mappings', 'Return trending hashtags', 'Delegate user search to auth-service', 'Delegate post search to post-service')
        Flows = @(
            'Index flow: post-service sends post id and content, search-service removes old mappings, extracts hashtags with regex, updates counts, and stores new mappings.',
            'Trending flow: repository returns top hashtags ordered by usage and recency so explore surfaces can show discovery data.',
            'Delegated search flow: instead of duplicating post or user storage, the service forwards search requests to the owning microservices.'
        )
        Integrations = @('Receives content indexing calls from post-service', 'Calls auth-service and post-service for delegated search', 'Feeds hashtag data into the frontend explore page')
        Endpoints = @('/api/v1/search/index', '/api/v1/search/posts', '/api/v1/search/users', '/api/v1/hashtags/trending', '/api/v1/hashtags/{tag}/posts')
        Verify = 'mvn test'
    },
    [pscustomobject]@{
        Name = 'chat-service'
        Title = 'Chat Service Architecture Guide'
        Type = 'Spring Boot microservice'
        Port = '8089'
        Database = 'Dedicated chat schema / chatdb in tests'
        Protocols = 'REST, STOMP over WebSocket/SockJS'
        Summary = @(
            'The chat-service adds realtime messaging to ConnectSphere. It stores conversations and messages through REST, then uses STOMP websocket topics to broadcast live chat and typing events.',
            'This keeps historical storage and live delivery in the same service boundary while still staying simple enough for a classroom-style microservice platform.'
        )
        Responsibilities = @('Create or fetch conversations', 'Persist chat messages', 'Return message history', 'Broadcast live messages', 'Broadcast typing indicators')
        Flows = @(
            'Conversation flow: two participant ids are submitted and the service either returns the existing pairwise conversation or creates a new one.',
            'REST history flow: the client can load conversations and prior messages through normal HTTP endpoints, which is useful after page refresh or reconnect.',
            'Realtime flow: the websocket controller receives `/app/chat.send`, saves the message, and publishes it to `/topic/chat.{conversationId}` so connected clients update immediately.',
            'Typing flow: the client publishes `/app/chat.typing` and subscribers receive lightweight typing events for the active conversation.'
        )
        Integrations = @('Frontend chat page uses both REST and websocket topics', 'Gateway proxies `/ws/chat/**` to this service', 'Could later integrate with notifications for offline delivery')
        Endpoints = @('/api/v1/chat/conversations', '/api/v1/chat/messages', '/ws/chat', '/app/chat.send', '/app/chat.typing')
        Verify = 'mvn test'
    },
    [pscustomobject]@{
        Name = 'api-gateway'
        Title = 'API Gateway Architecture Guide'
        Type = 'Spring Cloud Gateway module'
        Port = '8080'
        Database = 'None'
        Protocols = 'Reactive gateway routing, REST proxying, websocket proxying'
        Summary = @(
            'The API gateway is the single entry point for ConnectSphere backend traffic. It exposes stable paths for every service, applies global CORS policy, and routes websocket traffic to chat-service.',
            'It also forwards frontend-friendly routes to the Angular dev server so the platform can be assessed from one access point during development.'
        )
        Responsibilities = @('Route external requests to individual services', 'Expose one gateway origin for frontend calls', 'Handle global CORS configuration', 'Proxy websocket chat traffic', 'Keep service URLs hidden behind stable routes')
        Flows = @(
            'REST routing flow: a request to a versioned path like `/api/v1/posts/**` is matched by the configured route and proxied to the owning microservice.',
            'Frontend flow: browser requests for `/feed`, `/profile`, `/chat`, and static frontend paths are forwarded to the Angular dev server on port 4200.',
            'Realtime flow: websocket traffic under `/ws/chat/**` is forwarded to chat-service so the frontend can keep one gateway-facing websocket URL.'
        )
        Integrations = @('Routes every backend microservice', 'Acts as the frontend API origin', 'Critical to the "assess everything from one gateway" requirement')
        Endpoints = @('/api/v1/auth/**', '/api/v1/posts/**', '/api/v1/comments/**', '/api/v1/likes/**', '/api/v1/follows/**', '/api/v1/notifications/**', '/api/v1/stories/**', '/api/v1/search/**', '/api/v1/chat/**', '/ws/chat/**')
        Verify = 'mvn test'
    },
    [pscustomobject]@{
        Name = 'connectsphere-web'
        Title = 'Angular Frontend Architecture Guide'
        Type = 'Angular standalone frontend'
        Port = '4200 (served through gateway paths on 8080 during development)'
        Database = 'None'
        Protocols = 'Angular, HttpClient, router, STOMP/SockJS'
        Summary = @(
            'The Angular frontend is a lightweight assessment-ready client designed to stay readable while still touching all major platform services. It has a social-dashboard layout inspired by Twitter, Instagram, and Facebook, but keeps the code intentionally simple.',
            'The app contains feed, explore, profile, notifications, stories, and chat pages, plus a shared session panel that handles login and registration through auth-service.'
        )
        Responsibilities = @('Host a clean UI shell and route pages', 'Store auth session locally and send bearer tokens', 'Call backend services through the gateway', 'Render stories, notifications, follow suggestions, and realtime chat', 'Provide a simple but attractive microservice demo surface')
        Flows = @(
            'Session flow: the right-side shell logs users in or registers them through auth-service, stores the JWT pair in local storage, and refreshes the profile into reactive session state.',
            'Feed flow: the feed page loads followed-user relationships, requests post-service feed data, then orchestrates comment-service and like-service actions from one UI card.',
            'Explore flow: the page combines auth-service user search, post-service delegated search through search-service, trending hashtags, and follow suggestions.',
            'Stories flow: the stories page uploads multipart files to media-service and previews active stories returned from the service.',
            'Chat flow: the chat page uses REST endpoints for history and STOMP/SockJS for live conversation updates through the gateway websocket route.'
        )
        Integrations = @('Uses gateway-relative URLs everywhere', 'Uses auth-service for session state', 'Uses post/comment/like/follow/notification/media/search/chat services across pages', 'Uses proxy config for local Angular development')
        Endpoints = @('/feed', '/explore', '/profile', '/notifications', '/stories', '/chat', '/oauth2/authorization/google', '/oauth2/authorization/github')
        Verify = 'npm run build and npm test -- --watch=false'
    }
)

$chromePath = 'C:\Program Files\Google\Chrome\Application\chrome.exe'
if (-not (Test-Path $chromePath)) {
    throw "Chrome was not found at $chromePath"
}

foreach ($module in $modules) {
    $modulePath = Join-Path $PSScriptRoot $module.Name
    if (-not (Test-Path $modulePath)) {
        continue
    }

    $docsPath = Join-Path $modulePath 'docs'
    New-Item -ItemType Directory -Force -Path $docsPath | Out-Null

    $fileInventory = Get-FileRows -ModulePath $modulePath -ModuleName $module.Name
    $responsibilityList = ($module.Responsibilities | ForEach-Object { "<li>$(Escape-Html $_)</li>" }) -join "`r`n"
    $flowList = ($module.Flows | ForEach-Object { "<li>$(Escape-Html $_)</li>" }) -join "`r`n"
    $integrationList = ($module.Integrations | ForEach-Object { "<li>$(Escape-Html $_)</li>" }) -join "`r`n"
    $endpointList = ($module.Endpoints | ForEach-Object { "<code>$(Escape-Html $_)</code>" }) -join "<br>"
    $summaryParagraphs = ($module.Summary | ForEach-Object { "<p>$(Escape-Html $_)</p>" }) -join "`r`n"
    $generatedAt = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'

    $titleSlug = if ($module.Name -eq 'connectsphere-web') { 'connectsphere-web-frontend-guide' } else { "$($module.Name)-architecture-guide" }
    $htmlPath = Join-Path $docsPath "$titleSlug.html"
    $pdfPath = Join-Path $docsPath "$titleSlug.pdf"
    $fileUrl = 'file:///' + ($htmlPath -replace '\\', '/')

    $html = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$(Escape-Html $module.Title)</title>
    <style>
        :root {
            --ink: #1f3344;
            --muted: #627588;
            --line: #dce4eb;
            --panel: #f7fafc;
            --soft: #eaf2fb;
        }

        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: "Segoe UI", Arial, sans-serif;
            color: var(--ink);
            background: white;
        }

        .page {
            max-width: 1180px;
            margin: 0 auto;
            padding: 34px 38px 72px;
        }

        h1 {
            margin: 0 0 8px;
            font-size: 31px;
        }

        h2 {
            margin: 30px 0 10px;
            padding-bottom: 8px;
            border-bottom: 2px solid var(--line);
            font-size: 23px;
        }

        h3 {
            margin: 0 0 10px;
            font-size: 18px;
        }

        p, li {
            line-height: 1.6;
        }

        .lead {
            color: var(--muted);
            margin-bottom: 18px;
        }

        .summary-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 14px;
            margin: 16px 0 24px;
        }

        .card {
            background: var(--panel);
            border: 1px solid var(--line);
            border-radius: 14px;
            padding: 14px 16px;
        }

        .meta-table, .file-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 12px;
        }

        .meta-table th,
        .meta-table td,
        .file-table th,
        .file-table td {
            border: 1px solid var(--line);
            padding: 9px 10px;
            vertical-align: top;
            font-size: 13px;
            line-height: 1.55;
        }

        .meta-table th,
        .file-table th {
            background: var(--soft);
            text-align: left;
        }

        code {
            font-family: Consolas, "Courier New", monospace;
            font-size: 12px;
            white-space: pre-wrap;
            word-break: break-word;
        }

        ul {
            margin: 10px 0 0;
            padding-left: 20px;
        }

        @media (max-width: 900px) {
            .summary-grid {
                grid-template-columns: 1fr;
            }
        }

        @media print {
            body {
                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
            }
        }
    </style>
</head>
<body>
<div class="page">
    <h1>$(Escape-Html $module.Title)</h1>
    <p class="lead">
        Generated on $generatedAt from the current workspace. This guide explains the purpose, structure,
        internal working, service connections, and important files for <strong>$(Escape-Html $module.Name)</strong>.
    </p>
    $summaryParagraphs

    <h2>Architecture Snapshot</h2>
    <table class="meta-table">
        <tr><th style="width:24%">Module</th><td>$(Escape-Html $module.Name)</td></tr>
        <tr><th>Type</th><td>$(Escape-Html $module.Type)</td></tr>
        <tr><th>Port / Entry</th><td>$(Escape-Html $module.Port)</td></tr>
        <tr><th>Data Ownership</th><td>$(Escape-Html $module.Database)</td></tr>
        <tr><th>Protocols / Main Tech</th><td>$(Escape-Html $module.Protocols)</td></tr>
        <tr><th>Verification Command</th><td><code>$(Escape-Html $module.Verify)</code></td></tr>
        <tr><th>Key Endpoints or Routes</th><td>$endpointList</td></tr>
    </table>

    <div class="summary-grid">
        <section class="card">
            <h3>Responsibilities</h3>
            <ul>
                $responsibilityList
            </ul>
        </section>
        <section class="card">
            <h3>Integrations</h3>
            <ul>
                $integrationList
            </ul>
        </section>
    </div>

    <h2>Internal Working</h2>
    <section class="card">
        <ul>
            $flowList
        </ul>
    </section>

    <h2>File Inventory</h2>
    <p>
        The table below lists the important source, configuration, and test files that currently exist in the module.
        This is meant to help you explain how the module is connected internally when you present it or revise it later.
    </p>
    <table class="file-table">
        <thead>
            <tr>
                <th style="width:34%">File</th>
                <th>Description and why it matters</th>
            </tr>
        </thead>
        <tbody>
            $($fileInventory.Rows -join "`r`n")
        </tbody>
    </table>

    <h2>Assessment Notes</h2>
    <section class="card">
        <p>Current file inventory count: <strong>$($fileInventory.Count)</strong></p>
        <p>
            This guide focuses on how the module works end to end: what it owns, which requests it handles,
            what supporting files make it run, and how it connects to the rest of ConnectSphere.
        </p>
    </section>
</div>
</body>
</html>
"@

    Set-Content -Path $htmlPath -Value $html -Encoding UTF8
    & $chromePath --headless --disable-gpu --print-to-pdf="$pdfPath" "$fileUrl" | Out-Null
}

Write-Output 'Architecture docs generated successfully.'
