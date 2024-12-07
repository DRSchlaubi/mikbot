# kubernetes

Plugin providing tools for running Mikbot within kubernetes

Also shouteout to @lucsoft, who helped me to set this up

## kubernetes hooks

The plugin adds a HTTP endpoint, which you can use as a K8s probe, it will return 200 unless a shard or the DB connection
is down.
It also adds a pre-stop hook, which will call hooks of the [redeploy-hook](../redeploy-hook) plugin

```yaml
          livenessProbe:
              httpGet:
                  path: /healthz
                  port: mikbot-kubernetes # remember to specify this port somewhere, it defaults to 8081
          startupProbe:
              httpGet:
                  path: /healthz
                  port: mikbot-kubernetes
              # let's give our shards time to connect
              initialDelaySeconds: 15
          lifecycle:
              preStop:
                  httpGet:
                      port: mikbot-kubernetes
                      path: /kubernetes/pre-stop
```

### Own health checks
You can add more checks by extending the `HealthCheck` [extension point](../../PLUGINS.md#what-are-extension-points)

## Scaling

The bot supports very basic distribution of shards through a stateful set, here is an example configuration

The bot will automatically re-balance if enough new guilds are created that a new shard is required

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: test-bot
  namespace: default
spec:
  serviceName: test-bot
  replicas: 4
  ordinals:
    start: 0
  selector:
    matchLabels:
      app: test-bot
  template:
    metadata:
      labels:
        app: test-bot
    spec:
      nodeSelector:
        fast: "true"
      containers:
        - name: test-bot
          image: ghcr.io/drschlaubi/k8s-test-bot:latest
          imagePullPolicy: Always
          livenessProbe:
            httpGet:
              path: /healthz
              port: mikbot
          startupProbe:
            httpGet:
              path: /healthz
              port: mikbot
            initialDelaySeconds: 15
          ports:
            - containerPort: 8080
              name: mikbot
          env:
            - name: TOTAL_SHARDS
              value: "8"
            - name: ENABLE_SCALING
              value: "true"
            - name: SHARDS_PER_POD # default value is true
              value: "2"
            - name: POD_ID
              valueFrom:
                fieldRef:
                  fieldPath: metadata.labels['apps.kubernetes.io/pod-index']
```
