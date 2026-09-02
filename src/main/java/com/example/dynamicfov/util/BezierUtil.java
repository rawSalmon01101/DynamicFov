package com.example.dynamicfov.util;

public class BezierUtil 
{

    public static float evalBezier(String rawValues, float time) 
  {
        float x1 = 0.2f, y1 = 0.7f, x2 = 0.0f, y2 = 1.0f;

        try 
        {
            String[] parts = rawValues.split(",");
            if (parts.length == 4) {
                x1 = Float.parseFloat(parts[0].trim());
                y1 = Float.parseFloat(parts[1].trim());
                x2 = Float.parseFloat(parts[2].trim());
                y2 = Float.parseFloat(parts[3].trim());
          }
        } 
        catch (NumberFormatException ignored) 
        {
            // Fallback to default if string is malformed
        }

        // Newton-Raphson method to solve x(t) = time, then calculate y(t)
        float t = time;
        for (int i = 0; i < 5; i++) 
        {
            float currentX = getBezierValue(x1, x2, t) - time;
            float derivativeX = getBezierDerivative(x1, x2, t);
            if (Math.abs(derivativeX) < 1e-5) break;
            t -= currentX / derivativeX;
            t = Math.max(0.0f, Math.min(1.0f, t));
        }

        return Math.max(0.0f, Math.min(1.0f, getBezierValue(y1, y2, t)));
    }

    private static float getBezierValue(float p1, float p2, float t) 
    {
        float invT = 1.0f - t;
        return 3 * invT * invT * t * p1 + 3 * invT * t * t * p2 + t * t * t;
    }

    private static float getBezierDerivative(float p1, float p2, float t) 
    {
        float invT = 1.0f - t;
        return 3 * invT * invT * p1 + 6 * invT * t * (p2 - p1) + 3 * t * t * (1.0f - p2);
    }
}
