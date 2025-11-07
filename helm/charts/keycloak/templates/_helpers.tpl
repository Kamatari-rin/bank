{{- define "keycloak.name" -}}
{{- default .Chart.Name .Values.nameOverride -}}
{{- end -}}

{{- define "keycloak.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "keycloak.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}
