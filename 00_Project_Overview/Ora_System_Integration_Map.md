# Ora System Integration Map: Take-home Exercise Library → Ora System Sessions

## System primitives (what The Ora System can actually do)
### Session A — Video Therapy (phone/tablet only)
**Supports:** therapist-assigned instructional videos + passive tracking-quality logging  
**Logs (non-clinical):**
- video completion %
- face detected % (tracking quality)
- mouth movement event counts (events only)
- head motion magnitude (stability/comfort proxy)

### Session B — Gaming Therapy (Pod + Dongle on phone/console/PC)
**Supports:** jaw-gesture-controlled gameplay + participation/motor-activity logging  
**Logs (non-clinical):**
- jaw open/close event counts
- lateral movement counts (L/R)
- movement range proxy (relative amplitude; not calibrated)
- session duration + completion vs target
- optional consistency score (variance in timing/amplitude)

## Critical boundary (must stay explicit in product + clinician messaging)
- These metrics are **participation and activity-pattern logs**, not clinical assessments.
- Do **not** infer attention, swallow safety, tongue strength, lip seal strength, bite force, ROM in mm, or diagnosis.

---

## Exercise → Ora System mapping

> Legend: **Full fit** = The Ora System can drive the exercise *and* log meaningful volume metrics.  
> **Partial fit** = The Ora System can deliver content and/or log completion, but cannot measure the targeted physiology.

| Exercise (library)                                                          |           Mode |                            Fit | What The Ora System can do/log                                                                   | What The Ora System cannot claim/measure                              |
| --------------------------------------------------------------------------- | -------------: | -----------------------------: | ------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------- |
| **Jaw-Opening Exercise (JOE)** (sustained maximal opening)                  | Gaming + Video | Full (Gaming), Partial (Video) | Gaming: rep counts, duration, consistency; Video: assigned instruction + completion              | true jaw opening in mm; strength gains; swallow outcomes              |
| **High-speed jaw-opening training**                                         | Gaming + Video | Full (Gaming), Partial (Video) | Gaming: high-tempo rep engine + event rate; Video: instruction + completion                      | validated hyoid/UES effects; calibrated velocity                      |
| **Jaw ROM / mobility (trismus/TMJ)** (TheraBite, tongue-depressor stacking) |          Video |                        Partial | instructional videos + completion + tracking-quality flags                                       | stretch dose quality; ROM in mm; pain/safety monitoring               |
| **Rocabado-style control drills** (tongue-on-palate controlled opening)     | Video + Gaming |                        Partial | Video: instruction + completion; Gaming: can prompt controlled open/close cadence                | correct tongue posture; TMJ mechanics; clinical motor-control scoring |
| **Structured gum-chewing training**                                         | Gaming + Video |                        Partial | Gaming: chewing-like cadence counts and duration; Video: instruction + completion                | bite force; masticatory load; dental/TMJ safety                       |
| **Isometric mouthpiece clench training**                                    | Gaming + Video |                        Partial | Gaming: clench event counts + duration (if clench gesture used); Video: instruction + completion | occlusal force; masseter thickness/quality                            |
| **CTAR** (chin tuck against resistance)                                     |          Video |                        Partial | instructional videos + completion + tracking quality                                             | actual resistance, hold quality, neck safety                          |
| **Shaker** (head-lift)                                                      |          Video |                        Partial | instructional videos + completion + tracking quality                                             | true rep/hold verification; cervical safety; swallow physiology       |
| **Mendelsohn maneuver**                                                     |          Video |                 Partial (weak) | instructional videos + completion                                                                | correct laryngeal hold; swallow safety; physiology verification       |
| **Effortful pitch glide**                                                   |          Video |                        Partial | instructional videos + completion                                                                | vocal effort accuracy; laryngeal elevation measurement                |
| **Tongue-to-palate presses (ant/post)**                                     |          Video |                        Partial | instructional videos + completion                                                                | tongue pressure (kPa); placement accuracy                             |
| **IOPI-style lingual resistance training**                                  |          Video |                 Partial (weak) | instructional videos + completion                                                                | any IOPI-like biofeedback or quantified loading                       |
| **Manual tongue resistance (directional)**                                  |          Video |                        Partial | instructional videos + completion                                                                | force, direction accuracy, compensations                              |
| **Lip press / resisted lip hold / cheek puff**                              |          Video |                        Partial | instructional videos + completion                                                                | lip seal strength/endurance; oral containment performance             |
| **ASHA lip closure awareness / button-pull**                                |          Video |                        Partial | instructional videos + completion                                                                | quantified resistance, technique scoring                              |

---

## Therapist prescription model (works with both modes)

### Assignment fields (minimum)
- **Exercise block name** (e.g., “Jaw Opening Reps”)
- **Mode**: Video or Gaming
- **Content**: video URL(s) or game title/platform
- **Dose**: target duration + frequency (e.g., 20 min, 3×/week)
- **Goals (only from loggable metrics)**:
  - Video: completion %, face detected % threshold (data quality)
  - Gaming: duration, event count minimums, optional consistency threshold

### Example blocks
#### Block A — “Jaw Opening Reps”
- Mode: Gaming
- Dose: 15–20 min, 3×/week
- Goals: duration ≥ target; open/close events ≥ N; consistency ≥ threshold (optional)

#### Block B — “CTAR Instruction + Compliance”
- Mode: Video
- Dose: 10 min, 5×/week
- Goals: completion ≥ 90%; face detected % ≥ threshold (data quality only)

---

## Recommended v1 focus (highest signal / lowest claim risk)
1) **Gaming mode** for: jaw opening + lateral control + cadence-based chewing patterns  
2) **Video mode** for: everything else as prescribed content + completion/data-quality logging  
3) Keep clinician dashboard language framed as: **adherence + activity patterns**, not clinical performance.



**Source alignment (for your spec wording):** two session modes + what they log (Video and Gaming) and the “critical boundary” language are in the tech spec.  
**Exercise list coverage:** CTAR/Shaker/JOE/Mendelsohn/EPG; tongue; lip; chewing; jaw ROM sections are in the exercise library.
