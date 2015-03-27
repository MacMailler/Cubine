

attribute vec3 a_position;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
attribute vec2 a_texCoord0;
varying vec2 v_texCoords0;


attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
varying vec3 v_normal;

uniform vec4 u_cameraPosition;

uniform float u_time;

uniform float u_fogstr;
varying float v_fogstr;

varying float v_fog;
varying vec4 v_fogColor;
uniform vec4 u_fogColor;

attribute vec4 a_color;
varying vec3 blocklight;
varying float baselight;


void main()
{
    v_texCoords0 = a_texCoord0;

    float dx = 0.0;
    float dz = 0.0;

    vec3 randomPos;

    if(v_texCoords0.x == 0.375 && v_texCoords0.y == 0.0 && a_color.a == 0.0){
            dx = 0.1*sin( mod(a_position.x,3.5)+u_time );
            dz = 0.25*cos( mod(a_position.x,3.5)+u_time );
    }else{
        dx = 0.0;
        dz = 0.0;
    }

        randomPos.x = a_position.x + dx;
		randomPos.y = a_position.y;
		randomPos.z = a_position.z + dz;


    vec4 pos = u_worldTrans * vec4(randomPos, 1.0);
    gl_Position = u_projViewTrans * pos;


    vec3 normal = normalize(u_normalMatrix * a_normal);
    v_normal = normal;
    v_fogstr = u_fogstr;
    v_fogColor = u_fogColor;

    vec3 flen = u_cameraPosition.xyz - pos.xyz;
    float fog = dot(flen, flen) * u_cameraPosition.w;
    v_fog = min(fog, 1.0);

    baselight = -0.25;
    blocklight = a_color.rgb;

}