using System.Collections.Generic;
using UnityEngine;
using Unity.MLAgents;
using Unity.MLAgents.Sensors;

public class RollerAgent : Agent
{
	public float jumpPower = 8f;
	public override void Heuristic(float[] actionsOut)
	{
		actionsOut[0] = Input.GetAxis("Horizontal");
		actionsOut[1] = Input.GetAxis("Vertical");
	}


	Rigidbody rBody;

	void Start()
	{
		rBody = GetComponent<Rigidbody>();
	}

	bool isJumping;

	void Update()
	{
	}

	public Transform Target;
	public GameObject Reward;
	public float LimitTime;

	public override void OnEpisodeBegin()
	{


		if (this.transform.localPosition.y < 0)
		{
			// If the Agent fell, zero its momentum
			this.rBody.angularVelocity = Vector3.zero;
			this.rBody.velocity = Vector3.zero;
			this.transform.localPosition = new Vector3(0, 0.5f, 0);
		}

		soso.SetActive(true);
	}


	public override void CollectObservations(VectorSensor sensor)
	{
		// Target and Agent positions
		sensor.AddObservation(Target.localPosition);
		sensor.AddObservation(this.transform.localPosition);
		sensor.AddObservation(Reward.transform.localPosition);
		// Agent velocity
		sensor.AddObservation(rBody.velocity.x);
		sensor.AddObservation(rBody.velocity.z);

	}
	public float forceMultiplier = 2;

	public override void OnActionReceived(float[] vectorAction)
	{
        LimitTime -= Time.deltaTime;
		if (LimitTime <= 0)
		{
			EndEpisode();
		}

		SetReward(-0.01f);

		// Actions, size = 2 = 3
		Vector3 controlSignal = Vector3.zero;
		controlSignal.x = vectorAction[0];
		controlSignal.z = vectorAction[1];

        rBody.AddForce(controlSignal * forceMultiplier);

        // Rewards
        float distanceToTarget = Vector3.Distance(this.transform.localPosition, Target.localPosition);
		// Reached target
		// Fell off platform

		if (this.transform.localPosition.y < 0)
		{
			SetReward(-1.0f);
			EndEpisode();
		}

		if (distanceToTarget < 1.50f)
		{
			SetReward(1.0f);
			EndEpisode();
		}

	}

	void OnCollisionEnter(Collision collision)
	{

		if (collision.gameObject.CompareTag("bad"))
		{

			SetReward(-1.0f);
			EndEpisode();
		}

		if (collision.gameObject.CompareTag("Reward"))
		{
			Reward.SetActive(false);
			SetReward(0.5f);

		}

		if (collision.gameObject.CompareTag("wall"))
		{

            SetReward(-0.5f);
        }

	}
}
