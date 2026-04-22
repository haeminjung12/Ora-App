## SECTION 1: TECHNOLOGY AND VALUE PROPOSITION

### Problem and Need Statement

Cerebral Palsy (CP) is the most common motor disability in childhood, requiring care that spans a lifetime [10]. While early intervention is critical, the current “chain of care” breaks the moment the patient leaves the clinic. Speech-Language Pathologists (SLPs) prescribe daily Home Exercise Programs (HEPs) to maintain oral motor function for swallowing safety and drooling management, but these programs place an enormous burden on families. Traditional repetitive exercises often lead to behavioral resistance, and in pediatric feeding disorders, this distress can cause “learned feeding aversions” where children refuse to eat [14, 15].

Consequently, the “adherence gap” is massive. Research by Stone et al. (2003) revealed a disparity: while paper logs reported >90% compliance, objective electronic monitoring proved actual adherence was only 11% [2]. In the context of CP, nearly 90% of home prescriptions are likely being ignored. This failure drives up costs—lifetime care for a person with CP already exceeds $1.6 million [8]. There is a clear clinical need for a remote monitoring platform that objectively tracks function without disrupting daily life.

### Innovation and Technical Feasibility

We propose **The Ora System**, a zero-friction, multimodal platform that gamifies therapy and objectively tracks two complementary data tracks:

* **Track A: Passive Profiling (The “Blind Spot” Solution)**
  This track measures function when the child is not trying, providing the “Ecological Momentary Assessment” (EMA) clinicians desire [18, 19]. The child simply watches cartoons via our secure “Watch Party” app. Using the device’s front-facing camera and a local instance of Google AI Edge MediaPipe [20], the system tracks 478 facial landmarks in real time. Deep learning-based facial analysis has shown >90% accuracy in identifying relevant phenotypes [7]. To address HIPAA and COPPA regulations, we use a “Zero-Footprint” approach: no video is stored or transmitted. Only encrypted mathematical coordinates are extracted to generate Passive Biomarkers, such as a “Spasticity Index” (movement jerkiness) and resting posture analysis.

* **Track B: Active Capacity Building**
  This track utilizes a small, wireless IMU Pod attached to the jaw via a disposable adhesive patch translating the patients movements, from simple jaw opening and closing to more complex movements like lateral excursions and protraction/retraction, to game-ready inputs. The pod acts as a standard Bluetooth Human Interface Device (HID). Our firmware translates therapeutic motions (e.g., “Open Jaw 50%”) into system-level inputs (e.g., “Spacebar,” “Click”), allowing the child to play popular games like Mario Kart using their mouth as the controller. This generates active data—Range of Motion (ROM), repetition count, and hold time—where the game is the motivator, but the pod is the objective source of truth.

### Competitive Advantage

Current solutions force a choice between precision and accessibility. Clinical tools like the IOPI [11] and Digital Swallowing Workstation™ [13] are prohibitively expensive and unobtainable for home use. Low-tech solutions like Chew Tubes [12] provide zero data tracking. Furthermore, existing competitors require active, obtrusive participation that burdens the caregiver.

The Ora System is the only solution designed to fit into the child’s existing digital life. Track A utilizes the family’s existing tablet (zero hardware cost), and Track B replaces expensive medical equipment with a low-cost wearable. This removes financial and psychological barriers to care, minimizing the risk of feeding aversions while providing SLPs with the first-ever objective profile of passive oral motor function in the home.

### Development Progress

We are currently at TRL 1–2 (Basic Principles Observed/Design Phase). We have de-risked the engineering path: our system architecture is defined, and we are validating the efficiency of the MediaPipe Face Mesh algorithm on reference hardwares. We have also mapped our regulatory strategy, identifying the Tongueometer (K183187) as our primary predicate for a 510(k) pathway and mapping our software modules to IEC 62304 safety classes.

### IP Coverage

We have developed an Intellectual Property strategy to protect both the hardware and software components of the Ora System. We are drafting a provisional patent application to secure our priority date before public disclosure. Our IP strategy focuses on protecting the unique system-level integration of passive computer vision tracking with active wearable gaming control.

### Prior Art and Freedom to Operate (FTO)

We have conducted a preliminary patent search using Google Patents and USPTO databases. Our analysis indicates that while individual components of our system exist in the prior art, no single patent discloses our specific multimodal integration.

* **Existing Patents**: Prior art primarily focuses on invasive, clinic-only devices. For example, US Patent 6,702,765 (Tongue/Palate Pressure) and the Iowa Oral Performance Instrument (IOPI) (US K920091) rely on intra-oral bulbs and wired connections. Similarly, devices like the VitalStim Experia (US K070425) focus on electrical stimulation rather than gamified motor tracking.
* **Differentiation**: The Ora System is distinct because it is non-invasive and wireless. Unlike the “flexible substrate” sensors seen in US Patent 10,238,331 (Nestec SA), our Track A uses zero-contact computer vision (MediaPipe), and our Track B uses an external jaw-mounted IMU. This difference in sensing modality (optical/inertial vs. pressure/contact) supports novelty and FTO.

### Protection Strategy

We plan to file a utility patent covering the following unique claims:

1. **System Architecture**: The method of synchronizing passive facial landmark tracking (Track A) with active IMU-based game control (Track B) into a unified longitudinal profile.
2. **Therapeutic “Translation” Algorithms**: Algorithms that convert raw jaw kinematic data (IMU inputs) into standard Human Interface Device (HID) commands (e.g., converting “50% jaw open” into a “Spacebar” key press).
3. **Passive Biomarker Analytics**: The method of extracting clinical metrics (e.g., Spasticity Index, Resting Posture Score) from encrypted facial landmark coordinates without storing video data.

This approach protects not just the physical device, but the data insights that drive the recurring revenue model.

---

## SECTION 2: MARKET POTENTIAL

### Market Landscape

Home-based oral motor therapy is a foundational component of care for children with cerebral palsy (CP). CP is the most common motor disability in childhood, affecting 1 in 345 children in the U.S. [16]. This creates a Total Addressable Market (TAM) of approximately 500,000 children under age 18 nationwide requiring lifelong motor management.

Within this population, 80–85% exhibit oropharyngeal dysphagia or oral motor dysfunction (deficits in jaw control, lip closure, and tongue coordination) [17]. This translates to a Serviceable Addressable Market (SAM) of roughly 400,000 U.S. pediatric patients who are candidates for structured, ongoing oral motor therapy delivered in the home. While our initial focus is CP, expansion opportunities exist in adjacent populations, including children with developmental delays and adults undergoing neurorehabilitation for stroke or Traumatic Brain Injury (TBI).

### Customer Segment and Obtainable Market

While the end-user is the patient, our customer is the clinician. Speech-Language Pathologists (SLPs) are the primary prescribers of home therapy.

* **Target Customer**: Private Practice SLPs
* **Market Size**: Of the 206,000 certified SLPs in the U.S. [21], approximately 42,000 work in private practice [22]

We estimate a conservative initial penetration of 3% of these private practice SLPs (about 1,260 clinicians). With an average caseload of 15 CP patients per clinician, Ora’s Serviceable Obtainable Market (SOM) is estimated at approximately 18,900 active pediatric users. This entry point balances realism with clinical relevance.

### Customer Discovery and Validation

Our initial customer discovery process targeted a wide rage of stakeholders, interveiwing a total of 15 SLPs, patients, care proviors, parents, and related industry individuals. Our interactions validated two pain points:

1. **The “Black Box” of Home Care**: Clinicians rely on caregiver-reported paper logs or verbal updates, which are unreliable.
2. **Pressure for Outcomes**: Clinicians face increasing pressure from insurance payers to demonstrate objective progress, yet they lack data from the home environment.
3. **Feeding Aversion**: Patients with CP often have feeding aversion, even after being fully developing the motor skills for eating.

We confirmed that SLPs are seeking tools that integrate into existing workflows without significant training. Ora addresses this by providing objective adherence and performance data, reducing clinical uncertainty.

### Commercialization Strategy and Business Model

Ora will operate under a B2B2C model, with private practice clinics serving as primary customers and distribution channels.

* **Hardware (The “Razor & Blade”)**: Revenue through sales of the IMU-based therapy pod and periodic replacement of disposable adhesive patches.
* **Software (SaaS)**: Clinics pay a subscription fee for the clinician dashboard (analytics, progress tracking, reporting).
* **Reimbursement Alignment**: Adoption is supported by alignment with CPT Code 98980 (Remote Therapeutic Monitoring). By enabling objective tracking of adherence, Ora allows clinicians to bill for time spent reviewing patient data, reducing barriers to adoption.

### Competitive Positioning

The current market is polarized between high-cost clinical diagnostics and low-tech home aids.

* **Clinic-Based Systems (e.g., IOPI, Digital Swallowing Workstation)**: High precision but expensive and restricted to the clinic.
* **Low-Tech Home Aids (e.g., Chew Tubes)**: Affordable but provide zero data tracking or feedback.

Ora creates a new category by combining passive assessment (Track A) with active gamified performance (Track B). Unlike competitors that require invasive or repetitive maneuvers, Ora captures both natural movement and functional capacity in a low-friction home environment.

---

## SECTION 3: TEAM

Our team is comprised of Haemin Jung and Elijah Zapalac. We share a deep knowledge in product development, prototyping, and related technical skills. Haemin has an experience of running a company for 2 years equiping the team with the necce


Our team combines deep technical execution in microfluidics and embedded systems with product development support, and is guided by experienced commercialization and translational medicine mentors. The core team is based at Texas A&M and has hands-on experience building integrated hardware/software prototypes and validating them in real lab workflows.

---

## SECTION 4: WORK PLAN AND OUTCOMES
