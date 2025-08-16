struct BaseLight
{
	vec3 color;
	float intensity;
};

struct AmbientLight
{
	BaseLight base;
};

struct DirectionalLight
{
	BaseLight base;
	vec3 direction;
};

struct Attenuation
{
	float constant;
	float linear;
	float exponent;
};

struct PointLight
{
	BaseLight base;
	Attenuation attenuation;
	vec3 position;
};

struct Glow
{
	BaseLight base;
	float intensity;
	float affect;
};

vec4 calcBaseLight(BaseLight light)
{
	return (vec4(light.color,1.0) * light.intensity);
}

vec4 calcAmbientLight(AmbientLight light)
{
	return calcBaseLight(light.base);
}

float calcBrightness(vec4 color)
{
	return dot(color.rgb, vec3(0.2126,0.7152, 0.0722));
}

vec4 calcDirectionalLight(BaseLight light, vec3 dir,  vec3 normal)
{
	float diffuseFactor = dot(-dir, normal);
	
	vec4 diffuseColor = vec4(0,0,0,0);
	vec4 specularColor = vec4(0,0,0,0);
	
	if (diffuseFactor > 0)
	{
		diffuseColor = vec4(light.color, 1.0f) * light.intensity * diffuseFactor;
	}
	
	return diffuseColor;
}

float calcAttenuation(Attenuation attenuation, float distance)
{
	return attenuation.constant + attenuation.linear * distance + attenuation.exponent * distance * distance + 0.0001;
}

vec4 calcSpecularReflection(BaseLight light, vec3 dir, vec3 camPos, vec3 fragPos, vec3 normal,  float intensity, float power, float divident)
{
	float diffuseFactor = dot(-dir, normal);
	
	vec4 specularColor = vec4(0,0,0,0);
	
	if (diffuseFactor > 0)
	{
		vec3 directionToCamera = normalize(camPos - fragPos);
		vec3 reflectDirection = normalize(reflect(directionToCamera, normal));
		
		if(intensity > 0.0f)
		{
			float specularFactor = dot(directionToCamera, reflectDirection);
			specularFactor = pow(specularFactor, power);
			
			if(specularFactor > 0.0f)
			{
				specularColor = vec4(light.color, 1.0f) * intensity * specularFactor;
			}	
		}

	}
	
	return specularColor / divident;
}

vec4 calcPointLight(PointLight light, vec3 pos, vec3 normal)
{
	vec3 toVertex = pos - light.position;
	float distance = length(toVertex);
	float attenuation = calcAttenuation(light.attenuation, distance);
	
	vec4 l = calcDirectionalLight(light.base, toVertex, normal);
	
	return l / attenuation;
}

vec4 calcGlow(Glow glow, vec4 base)
{
	vec4 totalLight = calcBaseLight(glow.base);

	float intensity = calcBrightness(base);
	
	//return glow.intensity * (base + (glow.affect * (base * totalLight)));
	
	return glow.intensity * ((1.0f-glow.affect) * base + glow.affect * (intensity * totalLight));
}




