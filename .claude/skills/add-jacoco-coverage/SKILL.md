---
name: add-jacoco-coverage
description: Add JaCoCo code coverage to a Quarkus + Maven project — wires the jacoco-maven-plugin to instrument unit and integration tests, generates HTML + XML reports, enforces per-package coverage thresholds (line and branch) that fail the build below minimum, excludes DTOs and generated code from measurement, and adds a CI step to upload the report. Use when the user asks for code coverage, JaCoCo, coverage threshold, "measure how much my tests actually run", or wants a Codecov/SonarCloud integration.
---

# add-jacoco-coverage

Add JaCoCo with **enforced** thresholds — running it without `check` goals means the report exists but no PR fails when coverage drops, which means coverage drops.

## When to invoke

- "Add JaCoCo / code coverage"
- "Show me what my tests cover"
- "Block PRs that drop coverage"

## What this skill produces

- `target/site/jacoco/index.html` — visual report after `./mvnw verify`.
- `target/site/jacoco/jacoco.xml` — machine-readable, consumed by Codecov / SonarCloud / GitHub annotations.
- Build failure when **line coverage** in `service/` or `repository/` falls below the configured minimum.
- DTOs, entities (mostly getters/setters), and generated code excluded so the percentages reflect real logic.

## Inputs to collect

| Input | Default |
|---|---|
| Minimum line coverage (service+repository) | `80%` |
| Minimum branch coverage (service+repository) | `70%` |
| Minimum line coverage overall | `60%` (lower — accounts for DTOs/configs/etc.) |
| Exclude patterns | `**/dto/**`, `**/entity/**`, `**/config/**`, `**/*Application*`, `**/openapi/**` |
| Fail build on threshold violation? | yes — that's the point |
| Upload to Codecov / SonarCloud? | ask; not required |

## `pom.xml` plugin block

Add inside `<build><plugins>`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>

        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>

        <execution>
            <id>prepare-agent-it</id>
            <goals><goal>prepare-agent-integration</goal></goals>
        </execution>

        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals>
                <goal>report</goal>
                <goal>report-integration</goal>
            </goals>
        </execution>

        <execution>
            <id>check-coverage</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
            <configuration>
                <haltOnFailure>true</haltOnFailure>
                <rules>
                    <!-- Overall floor — keeps random regressions visible -->
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>{{overallLine}}</minimum>
                            </limit>
                        </limits>
                    </rule>
                    <!-- Service & repository packages — the real logic -->
                    <rule>
                        <element>PACKAGE</element>
                        <includes>
                            <include>{{packageRoot}}.service*</include>
                            <include>{{packageRoot}}.repository*</include>
                        </includes>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>{{logicLine}}</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>{{logicBranch}}</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>

    </executions>

    <configuration>
        <excludes>
            <exclude>**/dto/**</exclude>
            <exclude>**/entity/**</exclude>
            <exclude>**/config/**</exclude>
            <exclude>**/*Application*</exclude>
            <exclude>**/openapi/**</exclude>
            <!-- Quarkus generated -->
            <exclude>**/io/quarkus/**</exclude>
            <exclude>**/*_ClientProxy*</exclude>
            <exclude>**/*_Bean*</exclude>
            <exclude>**/*_Subclass*</exclude>
        </excludes>
    </configuration>
</plugin>
```

> **Critical**: `prepare-agent-integration` is what makes coverage include `@QuarkusTest` integration tests (run by failsafe), not just plain unit tests (run by surefire). Without it, REST Assured tests don't count.

## Surefire / failsafe argLine integration

JaCoCo's `prepare-agent` sets an `argLine` property. Surefire and failsafe configs in this project already use `<argLine>--add-opens java.base/java.lang=ALL-UNNAMED</argLine>` — that **overrides** JaCoCo's argLine and breaks coverage. Fix by referencing JaCoCo's property:

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>@{argLine} --add-opens java.base/java.lang=ALL-UNNAMED</argLine>
        <!-- ... rest unchanged ... -->
    </configuration>
</plugin>
<plugin>
    <artifactId>maven-failsafe-plugin</artifactId>
    <configuration>
        <argLine>@{argLine} --add-opens java.base/java.lang=ALL-UNNAMED</argLine>
        <!-- ... -->
    </configuration>
</plugin>
```

`@{argLine}` is Maven late-evaluation — picks up whatever JaCoCo set. Without this, the agent isn't attached and reports show 0%.

## How to read the report

After `./mvnw verify`:

- `target/site/jacoco/index.html` — overall summary, click into packages.
- `target/site/jacoco-it/` — integration tests separately (from `report-integration`).
- `target/site/jacoco/jacoco.xml` — XML for tooling.

Per-line annotations: green = covered, yellow = branch missed (some conditions untested), red = not executed at all.

## CI integration

Append to `.github/workflows/ci.yml` (the `build-and-test` job, after the `verify` step):

```yaml
      - name: Upload JaCoCo report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: target/site/jacoco/

      # Optional — Codecov upload
      - name: Upload to Codecov
        if: success()
        uses: codecov/codecov-action@v5
        with:
          files: target/site/jacoco/jacoco.xml
          fail_ci_if_error: false   # treat Codecov outages as non-blocking
```

> `fail_ci_if_error: false` — Codecov goes down occasionally; the local `jacoco:check` is the authoritative gate, not the upload.

## Threshold strategy

Don't set `90%` everywhere on day one. It either:
- forces useless tests (`@Test void itLoads() { new Album(); }`), or
- gets disabled by a frustrated developer.

Sensible progression:
1. **Day 1**: measure current coverage with `./mvnw verify`. Note the percentage.
2. **Threshold**: set `floor = current - 2%`. Build still passes today; ratchet up on each PR with new tests.
3. **Long-term target**: 80% line / 70% branch on `service+repository`. Don't chase 100% — diminishing returns.

Encode this in CLAUDE.md so future contributors don't blindly raise it.

## Anti-patterns to refuse

- **Counting DTO/getter coverage**. Pure data classes inflate the number without measuring anything. Always exclude.
- **Excluding service packages "until tests catch up"**. The whole point is to make that catch-up visible. Use a low threshold instead.
- **`<haltOnFailure>false</haltOnFailure>`** — defeats enforcement.
- **One threshold across all packages**. A 60% floor on resources is laughable; on entities meaningless. Per-package rules are the only honest way.
- **Pumping coverage with `assertNotNull(result)`** style tests. Coverage agent doesn't measure assertion quality — that's what mutation testing is for (separate skill `/add-mutation-testing`).

## Post-generation

- Run `./mvnw verify`. Open `target/site/jacoco/index.html`.
- Tell the user the current line / branch percentages so they can set realistic thresholds.
- Suggest pairing with `/add-mutation-testing` because high coverage with weak tests is the next failure mode.
