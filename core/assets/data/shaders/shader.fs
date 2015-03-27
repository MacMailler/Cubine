#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

varying LOWP vec2 v_texCoords0;
uniform sampler2D u_normalTexture;
uniform sampler2D u_diffuseTexture;

varying vec3 v_normal;

varying float v_fogstr;

varying vec4 v_fogColor;
varying float v_fog;

varying float baselight;
varying vec3 blocklight;

void main()
{
    vec3 light = blocklight.rgb+baselight;

    vec4 texColor = texture2D(u_diffuseTexture, v_texCoords0.xy).rgba;

    if(texColor.a < 0.5)
        discard;
    vec4 finalColor = vec4(texColor.xyz * light.rgb, texColor.a);

    const float LOG2 = 1.442695;
    float z = (gl_FragCoord.z / gl_FragCoord.w)/3.0;
    float fogFactor = exp2( -v_fogstr * v_fogstr * z * z * LOG2 );
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    gl_FragColor = mix(v_fogColor, finalColor, fogFactor );

}

