$ErrorActionPreference = "Stop"

Set-Location (Split-Path -Parent $PSScriptRoot)

function Escape-Html {
    param([string]$Text)

    if ($null -eq $Text) {
        return ""
    }

    return $Text.Replace('&', '&amp;').Replace('<', '&lt;').Replace('>', '&gt;').Replace('"', '&quot;')
}

function Normalize-Name {
    param([string]$Name)

    return ($Name -replace '([a-z0-9])([A-Z])', '$1 $2' -replace '_', ' ').Trim()
}

function Get-FilePurpose {
    param([string]$File)

    switch ($File) {
        'pom.xml' { return "Defines the Maven build, Java version, and all dependencies the auth service needs to compile, run, test, and create JWT-secured Spring Boot behavior." }
        'README.md' { return "Explains what the auth service does, what endpoints it exposes, and how to run it in development." }
        'src\main\java\com\connectsphere\auth\AuthServiceApplication.java' { return "Starts the Spring Boot application and enables loading JWT configuration properties from YAML." }
        'src\main\java\com\connectsphere\auth\config\JwtProperties.java' { return "Holds strongly typed JWT settings like issuer, secret, and token expiry durations, so configuration is safe and validated." }
        'src\main\java\com\connectsphere\auth\config\SecurityConfig.java' { return "Assembles Spring Security: public routes, protected routes, password hashing, authentication provider, stateless sessions, and JWT filter wiring." }
        'src\main\java\com\connectsphere\auth\controller\AuthResource.java' { return "Defines the REST API endpoints for registration, login, logout, refresh, token validation, profile management, search, and deactivation." }
        'src\main\java\com\connectsphere\auth\controller\GlobalExceptionHandler.java' { return "Converts Java exceptions into consistent HTTP responses so the API fails in a predictable, user-friendly way." }
        'src\main\java\com\connectsphere\auth\dto\ApiMessageResponse.java' { return "Simple output DTO used when an endpoint only needs to return a confirmation message." }
        'src\main\java\com\connectsphere\auth\dto\AuthResponse.java' { return "Represents the login/register/refresh response, including access token, refresh token, expiry times, and user profile data." }
        'src\main\java\com\connectsphere\auth\dto\ChangePasswordRequest.java' { return "Carries password-change input and validates that the required password fields are present and large enough." }
        'src\main\java\com\connectsphere\auth\dto\LoginRequest.java' { return "Carries login credentials into the service in a structured and validated way." }
        'src\main\java\com\connectsphere\auth\dto\LogoutRequest.java' { return "Carries the access token and optional refresh token that should be revoked during logout." }
        'src\main\java\com\connectsphere\auth\dto\RefreshTokenRequest.java' { return "Carries the refresh token used to request a new token pair." }
        'src\main\java\com\connectsphere\auth\dto\RegisterRequest.java' { return "Carries registration input and validates user-supplied data before the service creates a new account." }
        'src\main\java\com\connectsphere\auth\dto\TokenValidationRequest.java' { return "Wraps a token string for the validation endpoint so it can be checked by the service." }
        'src\main\java\com\connectsphere\auth\dto\TokenValidationResponse.java' { return "Returns whether a token is valid and, if so, which user and role it belongs to." }
        'src\main\java\com\connectsphere\auth\dto\UpdateProfileRequest.java' { return "Carries editable profile fields for profile updates and validates them before saving." }
        'src\main\java\com\connectsphere\auth\dto\UserProfileResponse.java' { return "Maps internal user data into a safe profile response without exposing sensitive fields like password hashes." }
        'src\main\java\com\connectsphere\auth\dto\UserSummaryResponse.java' { return "Represents the lighter user shape returned by search results." }
        'src\main\java\com\connectsphere\auth\entity\AuthProvider.java' { return "Lists the supported account providers so the service can distinguish local accounts from social-login accounts." }
        'src\main\java\com\connectsphere\auth\entity\RevokedToken.java' { return "Stores revoked JWT ids so logout and token rotation can invalidate tokens before they expire naturally." }
        'src\main\java\com\connectsphere\auth\entity\Role.java' { return "Defines the allowed authorization roles used by the auth service and downstream services." }
        'src\main\java\com\connectsphere\auth\entity\User.java' { return "Defines the main persisted user model, combining identity, profile, security, role, provider, and audit fields." }
        'src\main\java\com\connectsphere\auth\exception\BadRequestException.java' { return "Represents client-side mistakes such as invalid input or rejected business-rule conditions." }
        'src\main\java\com\connectsphere\auth\exception\NotFoundException.java' { return "Represents missing resources, mainly when the requested user cannot be found." }
        'src\main\java\com\connectsphere\auth\repository\RevokedTokenRepository.java' { return "Provides database access for revoked-token records so the service can check and purge them." }
        'src\main\java\com\connectsphere\auth\repository\UserRepository.java' { return "Provides all user lookup and search operations needed by authentication, validation, and profile logic." }
        'src\main\java\com\connectsphere\auth\security\AuthenticatedUser.java' { return "Adapts the application's User entity into Spring Security's UserDetails model so Spring can authenticate requests." }
        'src\main\java\com\connectsphere\auth\security\CustomUserDetailsService.java' { return "Loads a user from the database by email when Spring Security needs to authenticate credentials or restore identity." }
        'src\main\java\com\connectsphere\auth\security\JwtAuthenticationFilter.java' { return "Reads JWTs from incoming requests and, if valid, builds the authenticated Principal for protected endpoints." }
        'src\main\java\com\connectsphere\auth\security\JwtTokenService.java' { return "Creates, parses, validates, revokes, and rotates JWT access and refresh tokens." }
        'src\main\java\com\connectsphere\auth\service\AuthService.java' { return "Defines the business operations the auth module promises to support." }
        'src\main\java\com\connectsphere\auth\service\AuthServiceImpl.java' { return "Implements the real auth business logic by connecting repositories, security, hashing, validation, and token generation together." }
        'src\main\resources\application.yml' { return "Contains the default runtime configuration used in local development and tests, including H2 and JWT values." }
        'src\main\resources\application-mysql.yml' { return "Contains the alternative MySQL configuration for running the auth service against a real relational database." }
        'src\main\resources\application-oauth.yml' { return "Contains the profile-specific OAuth client settings used only when social login is enabled." }
        'src\test\java\com\connectsphere\auth\AuthResourceIntegrationTest.java' { return "Verifies the important auth flows through real HTTP-style integration tests so the module is proven to work end to end." }
        'src\test\java\com\connectsphere\auth\JsonTestHelper.java' { return "Provides a tiny helper for reading JSON values from test responses without repeating parsing code." }
        default { return "This file supports the auth-service implementation and is included so the document explains the module completely." }
    }
}

function Explain-JavaLine {
    param(
        [string]$Line,
        [string]$Trimmed,
        [string]$File
    )

    if ([string]::IsNullOrWhiteSpace($Trimmed)) { return "Blank line used to separate code blocks and improve readability." }
    if ($Trimmed -eq "{") { return "Opens the current code block." }
    if ($Trimmed -eq "}") { return "Closes the current code block." }
    if ($Trimmed -eq "};") { return "Closes the current block and ends the statement." }
    if ($Trimmed.StartsWith("//")) { return "Inline comment that explains the intent of the nearby code." }
    if ($Trimmed -match '^package\s+(.+);$') { return "Declares the Java package for this source file: $($matches[1])." }
    if ($Trimmed -match '^import\s+(.+);$') { return "Imports $($matches[1]) so the file can use it without writing the full package path each time." }
    if ($Trimmed -match '^@(.+)$') { return "Applies the annotation `$($matches[1])` to configure framework behavior or metadata for the next declaration; we need annotations in Spring and JPA so the framework knows how to wire, validate, persist, or expose this code." }
    if ($Trimmed -match '^(public\s+)?record\s+([A-Za-z0-9_]+)\(') { return "Declares the record `$($matches[2])`, which is useful here because DTOs are mostly immutable data carriers and records reduce boilerplate by generating accessors, equality, and constructor logic automatically." }
    if ($Trimmed -match '^(public\s+)?class\s+([A-Za-z0-9_]+)') { return "Declares the class `$($matches[2])`, the main implementation type defined in this file." }
    if ($Trimmed -match '^(public\s+)?interface\s+([A-Za-z0-9_]+)') { return "Declares the interface `$($matches[2])`, which defines a contract without storing implementation state; we need interfaces when we want clean abstraction or multiple implementations behind one API." }
    if ($Trimmed -match '^(public\s+)?enum\s+([A-Za-z0-9_]+)') { return "Declares the enum `$($matches[2])`, which defines a fixed set of allowed constants; enums are needed here to keep roles and providers constrained to valid values only." }
    if ($Trimmed -match '^(private|protected|public)\s+final\s+(.+?)\s+([A-Za-z0-9_]+);$') {
        return "Defines the field `$($matches[3])` of type `$($matches[2])`; because it is final, it is assigned once and then reused."
    }
    if ($Trimmed -match '^(private|protected|public)\s+(.+?)\s+([A-Za-z0-9_]+);$') {
        return "Defines the field `$($matches[3])` of type `$($matches[2])`, which stores state for this object."
    }
    if ($Trimmed -match '^public\s+([A-Za-z0-9_]+)\(') {
        return "Begins the constructor for `$($matches[1])`, which is used to create and initialize instances of the class."
    }
    if ($Trimmed -match '^(public|private|protected)\s+([A-Za-z0-9_<>, ?\[\]]+)\s+([A-Za-z0-9_]+)\((.*)\)\s*\{$') {
        return "Begins the method `$($matches[3])`, which returns `$($matches[2])` and contains the logic for this operation."
    }
    if ($Trimmed -match '^return\s+(.+);$') { return "Returns the value produced by the expression `$($matches[1])` to the caller." }
    if ($Trimmed -match '^throw\s+new\s+([A-Za-z0-9_]+)\((.*)\);$') { return "Throws `$($matches[1])` here so the request fails immediately with a clear error condition." }
    if ($Trimmed -match '^if\s*\((.+)\)\s*\{$') { return "Starts a conditional check; the block runs only when `$($matches[1])` is true." }
    if ($Trimmed -match '^else\s+if\s*\((.+)\)\s*\{$') { return "Starts an alternative conditional branch that runs when `$($matches[1])` is true." }
    if ($Trimmed -eq "else {") { return "Starts the fallback branch that runs when the earlier condition was false." }
    if ($Trimmed -match '^for\s*\((.+)\)\s*\{$') { return "Starts a loop that repeats using the control expression `$($matches[1])`." }
    if ($Trimmed -match '^this\.([A-Za-z0-9_]+)\s*=\s*([A-Za-z0-9_]+);$') { return "Assigns the value of `$($matches[2])` into the object field `$($matches[1])`." }
    if ($Trimmed -match '^([A-Za-z0-9_]+)\.save\((.+)\);$') { return "Calls the repository or service method `save(...)` to persist updated state; this is needed so in-memory changes are actually written to the database." }
    if ($Trimmed -match '^([A-Za-z0-9_]+)\.set([A-Za-z0-9_]+)\((.+)\);$') {
        $prop = Normalize-Name $matches[2]
        return "Updates the object's $prop value using the expression `$($matches[3])`."
    }
    if ($Trimmed -match '^String\s+([A-Za-z0-9_]+)\s*=\s*(.+);$') { return "Creates the local variable `$($matches[1])` and stores the result of `$($matches[2])`." }
    if ($Trimmed -match '^boolean\s+([A-Za-z0-9_]+)\s*=\s*(.+);$') { return "Creates the boolean variable `$($matches[1])` and sets it from `$($matches[2])`." }
    if ($Trimmed -match '^([A-Za-z0-9_<>, ?\[\]]+)\s+([A-Za-z0-9_]+)\s*=\s*(.+);$') { return "Creates the local variable `$($matches[2])` of type `$($matches[1])` and initializes it." }
    if ($Trimmed -match '^return\s+new\s+([A-Za-z0-9_]+)\(') { return "Builds and returns a new `$($matches[1])` object, which is usually needed here to produce a DTO or result object for the caller." }
    if ($Trimmed -match '^super\((.*)\);$') { return "Delegates part of the construction work to the parent class." }
    if ($Trimmed -match '^([A-Za-z0-9_]+)\((.*)\);$') { return "Calls the method `$($matches[1])` to continue the current workflow." }
    if ($Trimmed -match '^\);$') { return "Closes the multi-line method call or expression started above." }
    if ($Trimmed -match '^\)$') { return "Closes the parenthesized expression started above." }
    if ($Trimmed -match '^.+,$') { return "This line continues the parameter, argument, or collection list onto the next line." }
    if ($Trimmed -match '^.+\{$') { return "This line opens a new block for the declaration or statement it belongs to." }
    if ($Trimmed -match '^.+;$') { return "Executes or declares a single Java statement." }

    return "This line is part of the Java implementation for this file and contributes to the surrounding declaration or logic."
}

function Explain-YamlLine {
    param([string]$Trimmed)

    if ([string]::IsNullOrWhiteSpace($Trimmed)) { return "Blank line used to separate configuration sections." }
    if ($Trimmed.StartsWith("#")) { return "Comment that explains the related configuration setting." }
    if ($Trimmed -match '^([A-Za-z0-9_.-]+):\s*$') { return "Starts the YAML configuration section `$($matches[1])`." }
    if ($Trimmed -match '^([A-Za-z0-9_.-]+):\s+(.+)$') { return "Sets the configuration key `$($matches[1])` to `$($matches[2])`." }
    if ($Trimmed -match '^- (.+)$') { return "Adds `$($matches[1])` as one item in the current YAML list." }

    return "This line contributes to the YAML configuration structure."
}

function Explain-XmlLine {
    param([string]$Trimmed)

    if ([string]::IsNullOrWhiteSpace($Trimmed)) { return "Blank line used to separate XML sections." }
    if ($Trimmed -match '^<\?xml') { return "Declares the XML version and encoding used by this Maven file." }
    if ($Trimmed -match '^<([A-Za-z0-9:_-]+)(\s|>)') { return "Opens the XML element `$($matches[1])`, which defines part of the Maven build configuration." }
    if ($Trimmed -match '^</([A-Za-z0-9:_-]+)>$') { return "Closes the XML element `$($matches[1])`." }
    if ($Trimmed -match '^<([A-Za-z0-9:_-]+)>(.+)</([A-Za-z0-9:_-]+)>$') { return "Sets the XML element `$($matches[1])` to the value `$($matches[2])`." }

    return "This line forms part of the Maven XML configuration."
}

function Explain-MarkdownLine {
    param([string]$Trimmed)

    if ([string]::IsNullOrWhiteSpace($Trimmed)) { return "Blank line used to separate paragraphs or list sections." }
    if ($Trimmed -match '^#+\s+(.+)$') { return "Markdown heading that introduces the section `$($matches[1])`." }
    if ($Trimmed -match '^-\s+(.+)$') { return "Markdown bullet point that lists `$($matches[1])`." }
    if ($Trimmed -match '^```') { return "Starts or ends a fenced code block in the README." }

    return "Narrative documentation text for the module."
}

function Explain-Line {
    param(
        [string]$Line,
        [string]$File
    )

    $trimmed = $Line.Trim()

    switch -Regex ($File) {
        '\.java$' { return Explain-JavaLine -Line $Line -Trimmed $trimmed -File $File }
        '\.yml$'  { return Explain-YamlLine -Trimmed $trimmed }
        'pom\.xml$' { return Explain-XmlLine -Trimmed $trimmed }
        'README\.md$' { return Explain-MarkdownLine -Trimmed $trimmed }
        default { return "This line belongs to the file's content and supports the surrounding structure." }
    }
}

$files = rg --files -g '!target/**' |
    Where-Object {
        $_ -eq 'pom.xml' -or
        $_ -eq 'README.md' -or
        $_ -like 'src/*'
    } |
    Sort-Object

$rows = New-Object System.Collections.Generic.List[string]

$glossary = @(
    @{ Term = "Record"; Definition = "A Java type designed mainly for holding data. We use records for DTOs because they remove boilerplate constructors, getters, equality, and toString code." },
    @{ Term = "Principal"; Definition = "The currently authenticated identity attached to the request. In this service, Principal normally holds the logged-in user's email after the JWT filter authenticates the request." },
    @{ Term = "DTO"; Definition = "Data Transfer Object. A small object used to receive input from the client or return output to the client without exposing internal entities directly." },
    @{ Term = "Entity"; Definition = "A class mapped to a database table through JPA annotations. Entities represent persisted business data." },
    @{ Term = "Repository"; Definition = "The data-access layer. Repositories read and write entities from the database so service classes do not have to write SQL manually." },
    @{ Term = "Service"; Definition = "The business-logic layer. Services connect controllers, repositories, hashing, validation, tokens, and workflow rules." },
    @{ Term = "Controller"; Definition = "The HTTP layer. Controllers receive requests, call the service layer, and send responses back to the client." },
    @{ Term = "JWT"; Definition = "JSON Web Token. A signed token that carries identity and authorization data so the system can authenticate requests without server-side sessions." },
    @{ Term = "AuthenticationManager"; Definition = "A Spring Security component that verifies login credentials by delegating to configured authentication providers." },
    @{ Term = "PasswordEncoder"; Definition = "A Spring Security abstraction used to hash passwords safely and compare raw passwords against stored hashes." },
    @{ Term = "Filter"; Definition = "A component that runs before the controller on each request. The JWT filter checks the Authorization header and builds the authenticated security context." },
    @{ Term = "JPA"; Definition = "Java Persistence API. It lets Java classes map to relational tables using annotations and repositories." },
    @{ Term = "Stateless"; Definition = "A style where the server does not keep session state for each client. Here, auth state is carried by JWT tokens instead of HTTP session objects." },
    @{ Term = "OAuth"; Definition = "A standard for logging in through another provider like Google or GitHub. This auth service has the configuration base for that future flow." },
    @{ Term = "Enum"; Definition = "A type that restricts values to a fixed list, such as USER/ADMIN or LOCAL/GOOGLE/GITHUB." },
    @{ Term = "Interface"; Definition = "A contract that says what methods a type must provide, without storing state or implementation details." }
)

$glossaryRows = New-Object System.Collections.Generic.List[string]
foreach ($item in $glossary) {
    $glossaryRows.Add("<tr><td><strong>$(Escape-Html $item.Term)</strong></td><td>$(Escape-Html $item.Definition)</td></tr>")
}

foreach ($file in $files) {
    $lines = Get-Content $file
    $safeFile = Escape-Html $file
    $purpose = Escape-Html (Get-FilePurpose $file)
    $rows.Add("<section class='file-section'>")
    $rows.Add("<h2>$safeFile</h2>")
    $rows.Add("<div class='file-purpose'><strong>Why this file exists:</strong> $purpose</div>")
    $rows.Add("<p class='file-meta'>Line count: $($lines.Count)</p>")
    $rows.Add("<table>")
    $rows.Add("<thead><tr><th class='col-line'>Line</th><th class='col-code'>Code</th><th class='col-exp'>Explanation</th></tr></thead>")
    $rows.Add("<tbody>")

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $lineNumber = $i + 1
        $code = Escape-Html $lines[$i]
        if ([string]::IsNullOrEmpty($code)) {
            $code = "&nbsp;"
        }
        $explanation = Escape-Html (Explain-Line -Line $lines[$i] -File $file)
        $rows.Add("<tr><td class='line-no'>$lineNumber</td><td class='code'><code>$code</code></td><td class='exp'>$explanation</td></tr>")
    }

    $rows.Add("</tbody>")
    $rows.Add("</table>")
    $rows.Add("</section>")
}

$generatedAt = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

$html = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ConnectSphere Auth Service - Line by Line Explanation</title>
    <style>
        :root {
            --ink: #1a2533;
            --muted: #607286;
            --line: #d8dfe8;
            --panel: #f6f8fb;
            --accent: #164f86;
            --accent-soft: #e8f1fa;
        }

        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: "Segoe UI", Calibri, Arial, sans-serif;
            color: var(--ink);
            background: #fff;
        }

        .page {
            max-width: 1180px;
            margin: 0 auto;
            padding: 32px 36px 72px;
        }

        h1 {
            margin: 0 0 10px;
            font-size: 30px;
        }

        h2 {
            margin: 34px 0 8px;
            padding-bottom: 8px;
            border-bottom: 2px solid var(--line);
            font-size: 21px;
        }

        p {
            line-height: 1.55;
        }

        .lead {
            color: var(--muted);
            margin-bottom: 18px;
        }

        .summary {
            background: var(--panel);
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 14px 16px;
            margin: 18px 0 24px;
        }

        .file-purpose {
            background: #fffaf0;
            border: 1px solid #ead8ae;
            border-radius: 10px;
            padding: 10px 12px;
            margin: 10px 0 10px;
            line-height: 1.5;
        }

        .file-meta {
            color: var(--muted);
            font-size: 13px;
            margin: 4px 0 12px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            table-layout: fixed;
            margin-bottom: 20px;
        }

        th, td {
            border: 1px solid var(--line);
            vertical-align: top;
            padding: 8px 10px;
            font-size: 12px;
            line-height: 1.45;
        }

        th {
            background: var(--accent-soft);
            text-align: left;
        }

        .col-line { width: 6%; }
        .col-code { width: 40%; }
        .col-exp { width: 54%; }

        .line-no {
            text-align: right;
            color: var(--muted);
            white-space: nowrap;
        }

        code {
            font-family: Consolas, "Courier New", monospace;
            white-space: pre-wrap;
            word-break: break-word;
            display: block;
        }

        .exp {
            word-break: break-word;
        }

        @media print {
            body {
                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
            }

            .page {
                padding: 24px 26px 36px;
            }
        }
    </style>
</head>
<body>
<div class="page">
    <h1>ConnectSphere Auth Service - Line-by-Line Explanation</h1>
    <p class="lead">
        This document walks through the auth-service source line by line. Every listed source, configuration,
        documentation, and test file is explained in the same order that it appears in the project.
    </p>
    <div class="summary">
        <strong>Generated from actual source files.</strong><br>
        File count: $($files.Count)<br>
        Generated at: $generatedAt
    </div>
    <h2>Glossary and Definitions</h2>
    <p class="lead">
        This section defines some of the important Java and Spring words used throughout the auth-service code.
        If you meant Java <code>Principal</code> when you wrote "principle", that definition is included here too.
    </p>
    <table>
        <thead><tr><th style="width:22%">Term</th><th>Definition and why it matters here</th></tr></thead>
        <tbody>
            $($glossaryRows -join "`r`n")
        </tbody>
    </table>
    <h2>Why Access Token and Refresh Token Are Needed</h2>
    <p>
        The auth service uses two tokens because one token alone would force a bad tradeoff between security and user experience.
        An <strong>access token</strong> is the token sent with normal API requests. It proves who the user is and what role they have.
        It should not live forever, because if somebody steals it they could call protected endpoints as that user until it expires.
    </p>
    <p>
        A <strong>refresh token</strong> exists so the client can ask for a brand new access token without forcing the user to type
        their email and password again. This means the access token can stay shorter-lived for safety, while the refresh token keeps
        the login session practical for real use.
    </p>
    <table>
        <thead>
            <tr>
                <th style="width:22%">Token Type</th>
                <th>Why it exists</th>
                <th>How it is used in this auth service</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td><strong>Access Token</strong></td>
                <td>Used as the main working token on protected requests. We need it so every normal API call can be authenticated quickly without querying a server-side session store.</td>
                <td>Sent in the <code>Authorization: Bearer ...</code> header. Checked by <code>JwtAuthenticationFilter</code>. In this project it is configured to last <code>24 hours</code>.</td>
            </tr>
            <tr>
                <td><strong>Refresh Token</strong></td>
                <td>Used as the renewal token. We need it so users can stay signed in and receive a new access token after expiry without entering their password again.</td>
                <td>Sent only to <code>/auth/refresh</code>. Checked by <code>JwtTokenService.isRefreshTokenUsable(...)</code>. In this project it is configured to last <code>7 days</code>.</td>
            </tr>
        </tbody>
    </table>
    <p>
        If the system had <strong>only a long-lived access token</strong>, it would be convenient but risky, because a stolen token
        would stay useful for too long. If the system had <strong>only a short-lived access token</strong> and no refresh token,
        security would be stronger but users would need to log in again and again.
    </p>
    <p>
        Using both tokens solves both problems:
    </p>
    <ul>
        <li>Access token gives fast authentication for normal requests.</li>
        <li>Refresh token gives a safe way to renew access when the access token expires.</li>
        <li>The access token can be shorter-lived.</li>
        <li>The user does not have to repeatedly type credentials.</li>
    </ul>
    <p>
        This auth service also revokes refresh tokens during rotation. That means when the client uses a refresh token successfully,
        the old refresh token is revoked and a new token pair is created. We need this so token reuse becomes harder after leakage or theft.
    </p>
    <pre>Login
  -> server returns access token + refresh token

Normal protected API call
  -> client sends access token
  -> JWT filter validates it
  -> request continues

Access token expires
  -> protected request can no longer use it

Refresh flow
  -> client sends refresh token to /auth/refresh
  -> service validates refresh token
  -> service revokes old refresh token
  -> service returns new access token + new refresh token</pre>
    $($rows -join "`r`n")
</div>
</body>
</html>
"@

$outputPath = Join-Path $PSScriptRoot "auth-service-line-by-line-explanation.html"
Set-Content -Path $outputPath -Value $html -Encoding UTF8
Write-Output $outputPath
