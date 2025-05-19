#version 120

varying vec4 texcoord;
uniform sampler2D gcolor;

void main() {
     vec3 color = texture2D(gcolor, texcoord.st).rgb;
     float grayscale = dot(color, vec3(0.299, 0.587, 0.114));
     gl_FragColor = vec4(vec3(grayscale), 1.0);
}
